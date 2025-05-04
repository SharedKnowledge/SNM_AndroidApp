package net.sharksystem.sharknetmessengerandroid.sharknet


import android.content.Context
import net.sharksystem.SharkPeer
import net.sharksystem.SharkPeerFS
import net.sharksystem.asap.android.Util


class SharkNetApp {
    private var peer: String
    private var sharkPeer: SharkPeer
    companion object {
        private const val APP_FOLDER_NAME = "SharkNetMessenger_AppData"
        public lateinit var singleton: SharkNetApp

        fun initialize(context: Context, peer: String) {
            singleton = SharkNetApp(context, peer)
        }

        fun getPeer() : SharkPeer {
            return singleton.getPeer()
        }
    }
    constructor(context: Context, peer: String) {
        this.peer = peer
        // produce folder
        val rootDir = Util.getASAPRootDirectory(context, APP_FOLDER_NAME, peer)
        // produce application side shark peer
        this.sharkPeer = SharkPeerFS(peer, rootDir.absolutePath)
    }

    fun getPeer() : SharkPeer {
        return this.sharkPeer
    }
}