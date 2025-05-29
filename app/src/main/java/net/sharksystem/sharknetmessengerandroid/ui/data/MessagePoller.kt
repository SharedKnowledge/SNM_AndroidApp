package net.sharksystem.sharknetmessengerandroid.ui.data

import kotlinx.coroutines.*
import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessageList
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl

class MessagePoller(
    //@todo add this point a
    //  MessageReceivedListener listener = new MessageReceivedListener(SharkNetMessengerApp);
    //  should be implemented, but the SharkNetMessengerApp.class is only implemented for the cli ad this point
    //  instead doing a polling

    private val uri: CharSequence,
    private val pollIntervalMillis: Long = 2000L, // every 2 seconds
    private val onNewMessages: (List<SharkNetMessage>) -> Unit
) {
    private var lastMessageCount = 0
    private var pollingJob: Job? = null

    fun start() {
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val sharkMessengerComponent =
                        (SharkNetApp.singleton?.getPeer()
                            ?.getComponent(SharkNetMessengerComponent::class.java)
                                as? SharkNetMessengerComponentImpl)

                    val messageList: SharkNetMessageList? = sharkMessengerComponent?.getChannel(uri)?.messages
                    if (messageList != null) {
                        val currentSize = messageList.size()
                        if (currentSize > lastMessageCount) {
                            val newMessages = mutableListOf<SharkNetMessage>()
                            for (i in lastMessageCount until currentSize) {
                                val message = messageList.getSharkMessage(i, true)
                                newMessages.add(message)
                            }
                            lastMessageCount = currentSize
                            onNewMessages(newMessages)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(pollIntervalMillis)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
