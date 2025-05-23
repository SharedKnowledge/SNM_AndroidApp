package net.sharksystem.pki

import net.sharksystem.SharkPeer
import net.sharksystem.asap.crypto.InMemoASAPKeyStore
import net.sharksystem.sharknetmessengerandroid.sharknet.AndroidASAPKeyStoreNew


internal class AndroidSharkPKIComponentImpl : SharkPKIComponentImpl {
    var asapKeyStore: InMemoASAPKeyStore? = null
    constructor(owner: SharkPeer) : super(owner)
    fun setASAPKeyStore(keyStore: AndroidASAPKeyStoreNew) {
        this.asapKeyStore = keyStore
    }
}
