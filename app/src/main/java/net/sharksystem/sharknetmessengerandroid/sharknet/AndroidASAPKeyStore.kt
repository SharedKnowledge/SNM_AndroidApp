package net.sharksystem.sharknetmessengerandroid.sharknet

import android.content.Context
import net.sharksystem.asap.ASAPSecurityException
import net.sharksystem.asap.crypto.InMemoASAPKeyStore

class AndroidASAPKeyStore : InMemoASAPKeyStore {
    private var context: Context

    constructor(context: Context, peerID: String) : super(peerID) {
        this.context = context
        // re-load - if possible
        try {
            this.reloadKeys(context)
        } catch(e: ASAPSecurityException) {
            // no keys yet
            this.generateKeyPair()
        }
    }

    private fun reloadKeys(context: Context) {

    }

}