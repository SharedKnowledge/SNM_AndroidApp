package net.sharksystem.sharknetmessengerandroid.helper

import android.content.Context
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.hub.peerside.ASAPHubManager
import net.sharksystem.pki.PKIHelper.sixDigitsToString
import net.sharksystem.ui.messenger.cli.SharkNetMessengerApp

//hallo cemre
class SHARK {
    var snma: SharkNetMessengerApp
    var messenger: SharkNetMessengerComponent

    companion object {
        const val syncWithOthersInSeconds = ASAPHubManager.DEFAULT_WAIT_INTERVAL_IN_SECONDS
    }

    constructor(peer_name: String) {
        this.snma = SharkNetMessengerApp(peer_name, syncWithOthersInSeconds, System.out, System.err)
        this.messenger = snma.sharkMessengerComponent
    }

    fun openTCP(port: Int) {
        this.snma.openTCPConnection(port)
    }

    fun transformDigits(test: Int, context: Context) : String {
        return sixDigitsToString(test)
    }

}