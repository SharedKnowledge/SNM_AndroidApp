package net.sharksystem.sharknetmessengerandroid.sharknet


import android.content.Context
import net.sharksystem.SharkPeer
import net.sharksystem.SharkPeerFS
import net.sharksystem.asap.android.Util


class SharkNetApp {
    private var peer_name: String
    private var sharkPeer: SharkPeer
    companion object {
        private val APP_FOLDER_NAME = "SharkNetMessenger_AppData"
        private lateinit var singleton: SharkNetApp
    }
    constructor(context: Context, peer_name: String) {
        this.peer_name = peer_name
        // produce folder
        val rootDir = Util.getASAPRootDirectory(context, APP_FOLDER_NAME, peer_name)
        // produce application side shark peer
        this.sharkPeer = SharkPeerFS(peer_name, rootDir.getAbsolutePath())
    }
}