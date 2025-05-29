package net.sharksystem.sharknetmessengerandroid.ui.data

import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.ui.messenger.cli.MessageReceivedListener

//todo
//

class SharkData {
    companion object {
        fun initialize() {

        }
        fun reloadMessages(uri: CharSequence): List<SharkNetMessage> {
            val messages = mutableListOf<SharkNetMessage>()
            val sharkNetMessages = (SharkNetApp.Companion.singleton?.
            getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
                    as? SharkNetMessengerComponentImpl)?.
            getChannel(uri)?.
            getMessages()

            val size = sharkNetMessages!!.size()
            for (i in 0 until size) {
                val message = sharkNetMessages.getSharkMessage(i, true)
                messages.add(message)
            }

            return messages
        }
    }
}