package net.sharksystem.sharknetmessengerandroid.sharknet


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import net.sharksystem.SharkPeer
import net.sharksystem.SharkPeerFS
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentFactory
import net.sharksystem.asap.ASAP
import net.sharksystem.asap.android.Util
import net.sharksystem.asap.android.apps.ASAPAndroidPeer
import net.sharksystem.pki.SharkPKIComponent


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
            if (singleton == null) {
                singleton = SharkNetApp(context, peerName)
            }
        }
    }
    /**
     * Constructor for [SharkNetApp]. Creates a SharkPeer instance and the app data folder.
     *
     * @param context The Android context.
     * @param peerName The name of the peer.
     */
    private constructor(context: Context, peerName: String) {
        if (peerName == "") throw IllegalArgumentException("peerName must not be empty")

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
        //@todo filetransfer connection to that
        // produce application side shark peer
        this.sharkPeer = SharkPeerFS(this.peerName, rootDir.absolutePath)
        ///////////////////////////// SETUP PKI /////////////////////////////
        // create Android specific key store
        // @todo AndroidASAPKeyStore (like SharkNet2Android) net.sharksystem.sharknet.AndroidASAPKeyStore
        // create PKI Component Factory and add as Component
//        val pkiComponentFactory = SharkPKIComponentFactory()
//        // register this component with shark peer
//        singleton!!.sharkPeer.addComponent(
//            pkiComponentFactory, SharkPKIComponent::class.java
//        )
//        val sharkPKI =
//            singleton!!.sharkPeer.getComponent(SharkPKIComponent::class.java) as SharkPKIComponent?
        // create Messenger Component Factory and add as Component
        // create messenger factory - needs a pki
        // get messenger factory with pki component as parameter.
//        val messengerFactory =
//            SharkNetMessengerComponentFactory(
//                singleton!!.sharkPeer
//                    .getComponent(SharkPKIComponent::class.java) as SharkPKIComponent?
//            )
//        // register this component with shark peer
//        singleton!!.sharkPeer.addComponent(
//            messengerFactory, SharkNetMessengerComponent::class.java
//        )
        // initialize Peer
        // setup android (application side peer)
//        ASAPAndroidPeer.initializePeer(
//            peerID,
//            singleton!!.sharkPeer.formats,
//            APP_FOLDER_NAME,
//            context as Activity? //@todo casting correct?
//        )
//        // TODO - need to inject keystore
//
//        // launch service side
//        val applicationSideASAPPeer = ASAPAndroidPeer.startPeer(context)
//        // remember
//        singleton!!.setApplicationSideASAPAndroidPeer(applicationSideASAPPeer)
//        // start peer
//        // use asap peer proxy for this app side shark peer
//        singleton!!.sharkPeer.start(applicationSideASAPPeer)
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