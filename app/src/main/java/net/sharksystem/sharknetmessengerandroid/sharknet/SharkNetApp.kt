package net.sharksystem.sharknetmessengerandroid.sharknet


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import net.sharksystem.SharkPeer
import net.sharksystem.SharkPeerFS
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentFactory
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.asap.ASAP
import net.sharksystem.asap.android.Util
import net.sharksystem.asap.android.apps.ASAPAndroidPeer
import net.sharksystem.pki.AndroidSharkPKIComponentFactory
import net.sharksystem.pki.AndroidSharkPKIComponentImpl
import net.sharksystem.pki.SharkPKIComponent
import net.sharksystem.asap.ASAPSecurityException
import java.security.PrivateKey

/**
 * The main application class for SharkNet, responsible for initializing
 * and accessing the [SharkPeer].
 */

class SharkNetApp {
    private var sharkPeer: SharkPeer
    private lateinit var peerName: String
    private lateinit var peerID: String
    private var asapAndroidPeer: ASAPAndroidPeer? = null

    companion object {
        private const val APP_FOLDER_NAME = "SNM_AppData"
        private const val PREFERENCES_FILE = "SNM_Identity"
        private const val PF_PEER_NAME = "SNM_Identity_PeerName"
        private const val PF_PEER_ID = "SNM_Identity_PeerID"

        /**
         * Singleton instance of [SharkNetApp].
         */
        var singleton: SharkNetApp? = null

        /**
         * Initializes the [SharkNetApp] with the given context and peer name.
         *
         * @param context The Android context, typically `applicationContext`.
         * @param peerName The name of the peer (e.g., user ID or alias).
         */
        fun initialize(context: Context, peerName: String) {
            if (peerName == "") throw IllegalArgumentException("peerName must not be empty")
            Log.d("SharkDebug", "SharkNet App initialize called $peerName")
            if (singleton == null) {
                singleton = SharkNetApp(context, peerName)
                Log.d("SharkDebug", "SharkNet App created called $peerName")
            }
        }

        fun load(context: Context) : Boolean{
            val sharedPref: SharedPreferences = context.getSharedPreferences(
                PREFERENCES_FILE, Context.MODE_PRIVATE
            )
            val existingPeerName = sharedPref.getString(PF_PEER_NAME, null)
            Log.d("SharkDebug", "SharkNet App load called shared preferences - $existingPeerName")
            if (existingPeerName != null ) {
                singleton = SharkNetApp(context, existingPeerName)
                Log.d("SharkDebug", "SharkNet App created called shared preferences - $existingPeerName")
                return true
            } else {
                return false
            }
        }

        fun getMessengerComponent() : SharkNetMessengerComponent? {
            return (singleton!!.getPeer().getComponent(SharkNetMessengerComponent::class.java)
                    as? SharkNetMessengerComponent)
        }

        fun getPeerName(peerID: CharSequence) : String {
            if (peerID == singleton!!.peerID) return singleton!!.getPeer().sharkPeerName.toString()
            val sharkPKI = singleton!!.getPeer()
                .getComponent(SharkPKIComponent::class.java) as AndroidSharkPKIComponentImpl
            //throws ASAPSecurityException, if Peer not found
            return sharkPKI.getPersonValuesByID(peerID).name.toString()
        }

        fun getPeerNameWithID(peerID: CharSequence) : String {
            return try {
                "${getPeerName(peerID)}_${peerID}"
            } catch (_: ASAPSecurityException) {
                "$peerID"
            }
        }

        fun dummymethod() {
            //val pki =  singleton?.getPeer()?.getComponent(SharkPKIComponent::class.java) as AndroidSharkPKIComponentImpl?
            //Log.d("SharkDebug",pki?.numberOfPersons.toString())
            getMessengerComponent()!!.createChannel("sn://snm_14_10_1","sn://snm_android_test3")
            //val sharkPKI = SharkNetApp.singleton?.sharkPeer?.getComponent(SharkPKIComponent::class.java) as AndroidSharkPKIComponentImpl?
            //sharkPKI?.createNewKeyPair()
        }

    }
    /**
     * Constructor for [SharkNetApp]. Creates a SharkPeer instance and the app data folder.
     *
     * @param context The Android context.
     * @param peerName The name of the peer.
     */
    private constructor(context: Context, peerName: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            PREFERENCES_FILE, Context.MODE_PRIVATE
        )
        // Edits shared Preferences File
        sharedPref.edit {
            // save peerName
            this@SharkNetApp.peerName = peerName
            putString(PF_PEER_NAME, peerName)
            // read peerID or create
            val existingPeerID = sharedPref.getString(PF_PEER_ID, null)
            this@SharkNetApp.peerID = if (existingPeerID != null) {
                existingPeerID
            } else {
                val newID = ASAP.createUniqueID()
                putString(PF_PEER_ID, newID)
                newID
            }
        }
        // produce folder
        val rootDir = Util.getASAPRootDirectory(context, APP_FOLDER_NAME, this.peerName)
        // produce application side shark peer
        this.sharkPeer = SharkPeerFS(this.peerName, rootDir.absolutePath)
        //////////////////////// SETUP PKI /////////////////////////////////////////////
        // create Android specific key store
        val androidASAPKeyStore = AndroidASAPKeyStoreNew(context,this.peerID)
        // create PKI Component Factory and add as Component
        //val pkiComponentFactory = SharkPKIComponentFactory()
        val pkiComponentFactory = AndroidSharkPKIComponentFactory(context,this.peerID)
        // register this component with shark peer
        this.sharkPeer.addComponent(
            pkiComponentFactory, SharkPKIComponent::class.java
        )
        val sharkPKI = this.sharkPeer.getComponent(SharkPKIComponent::class.java) as AndroidSharkPKIComponentImpl?
        sharkPKI?.setASAPKeyStore(androidASAPKeyStore)

        //////////////////////// SETUP MESSENGER /////////////////////////////////////////////
        // create Messenger Component Factory and add as Component
        // create messenger factory - needs a pki
        // get messenger factory with pki component as parameter.
        val messengerFactory =
            SharkNetMessengerComponentFactory(
                this.sharkPeer
                    .getComponent(SharkPKIComponent::class.java) as SharkPKIComponent?
            )
        // register this component with shark peer
        this.sharkPeer.addComponent(
            messengerFactory, SharkNetMessengerComponent::class.java
        )
        //////////////////////// INIT ANDROID PEER /////////////////////////////////////////////

        // initialize Peer
        // setup android (application side peer)
        ASAPAndroidPeer.initializePeer(
            peerID,
            this.sharkPeer.formats,
            APP_FOLDER_NAME,
            context as? Activity //because this is called
        )


        // launch service side
        val applicationSideASAPPeer = ASAPAndroidPeer.startPeer(context as? Activity)
        // remember
        this.setApplicationSideASAPAndroidPeer(applicationSideASAPPeer)
        // start peer
        // use asap peer proxy for this app side shark peer
        this.sharkPeer.start(applicationSideASAPPeer)

    }

    /**
     * Returns the internal [SharkPeer] instance.
     *
     * @return The [SharkPeer] instance of this app.
     */
    fun getPeer() : SharkPeer {
        return this.sharkPeer
    }

    private fun setApplicationSideASAPAndroidPeer(asapAndroidPeer: ASAPAndroidPeer?) {
        this.asapAndroidPeer = asapAndroidPeer
    }
}
