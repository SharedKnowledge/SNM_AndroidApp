package net.sharksystem.pki

import android.content.Context
import net.sharksystem.SharkComponent
import net.sharksystem.SharkException
import net.sharksystem.SharkPeer
import net.sharksystem.asap.crypto.ASAPKeyStore
import net.sharksystem.sharknetmessengerandroid.sharknet.AndroidASAPKeyStoreNew
import java.io.IOException

class AndroidSharkPKIComponentFactory(
    private val context: Context,
    private val peerID: String
) : SharkPKIComponentFactory() {

    private var asapKeyStore: ASAPKeyStore? = null
    private var instance: AndroidSharkPKIComponentImpl? = null

    @Throws(SharkException::class)
    override fun getComponent(sharkPeer: SharkPeer): SharkComponent {
        if (this.instance == null) {
            try {
                this.instance = AndroidSharkPKIComponentImpl(sharkPeer)
                this.asapKeyStore = AndroidASAPKeyStoreNew(context,this.peerID)
                this.instance?.setASAPKeyStore(this.asapKeyStore as AndroidASAPKeyStoreNew)
            } catch (e: IOException) {
                throw SharkException(e)
            }
        }

        return this.instance!!
    }
}