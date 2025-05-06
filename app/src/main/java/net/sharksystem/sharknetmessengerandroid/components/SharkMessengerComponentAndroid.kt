package net.sharksystem.sharknetmessengerandroid.components

import net.sharksystem.SharkException
import net.sharksystem.SharkNotSupportedException
import net.sharksystem.app.messenger.InMemoSharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessagesReceivedListenerManager
import net.sharksystem.app.messenger.SharkNetMessengerChannel
import net.sharksystem.app.messenger.SharkNetMessengerChannelImpl
import net.sharksystem.app.messenger.SharkNetMessengerClosedChannel
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerException
import net.sharksystem.asap.ASAPException
import net.sharksystem.asap.ASAPHop
import net.sharksystem.asap.ASAPMessageReceivedListener
import net.sharksystem.asap.ASAPMessages
import net.sharksystem.asap.ASAPPeer
import net.sharksystem.asap.ASAPStorage
import net.sharksystem.pki.SharkPKIComponent
import net.sharksystem.utils.Log
import java.io.IOException

class SharkNetMessengerComponentImpl(private val sharkPKIComponent: SharkPKIComponent) :
    SharkNetMessagesReceivedListenerManager(), SharkNetMessengerComponent,
    ASAPMessageReceivedListener {
    private var asapPeer: ASAPPeer? = null

    @Throws(SharkException::class)
    override fun onStart(asapPeer: ASAPPeer) {
        this.asapPeer = asapPeer

        try {
            this.createChannel("sn://universal", "sharkNet")
        } catch (ex: IOException) {
            throw SharkException(ex)
        }

        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)")
        this.asapPeer!!.addASAPMessageReceivedListener("shark/messenger", this)
    }

    @Throws(SharkNetMessengerException::class)
    private fun checkComponentRunning() {
        if (this.asapPeer == null || this.sharkPKIComponent == null) {
            throw SharkNetMessengerException("peer not started an/or pki not initialized")
        }
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    override fun sendSharkMessage(
        contentType: String?,
        content: ByteArray?,
        uri: CharSequence?,
        sign: Boolean
    ) {
        val set: HashSet<CharSequence?> = HashSet<CharSequence?>()
        this.sendSharkMessage(contentType, content, uri, set as MutableSet<*>, sign, false)
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    override fun sendSharkMessage(
        contentType: String?,
        content: ByteArray?,
        uri: CharSequence?,
        receiver: CharSequence?,
        sign: Boolean,
        encrypt: Boolean
    ) {
        val set: HashSet<CharSequence?> = HashSet<CharSequence?>()
        set.add(receiver)
        this.sendSharkMessage(contentType, content, uri, set as MutableSet<*>, sign, encrypt)
    }

    @Throws(SharkNetMessengerException::class, IOException::class)
    override fun sendSharkMessage(
        contentType: String?,
        content: ByteArray?,
        uri: CharSequence?,
        selectedRecipients: MutableSet<CharSequence?>?,
        sign: Boolean,
        encrypt: Boolean
    ) {
        this.checkComponentRunning()

        try {
            if (encrypt && selectedRecipients != null && selectedRecipients.size > 1) {
                for (receiver in selectedRecipients) {
                    this.asapPeer!!.sendASAPMessage(
                        "shark/messenger",
                        uri,
                        InMemoSharkNetMessage.serializeMessage(
                            contentType,
                            content,
                            this.asapPeer!!.peerID,
                            receiver,
                            sign,
                            encrypt,
                            this.sharkPKIComponent.asapKeyStore
                        )
                    )
                }
            } else {
                this.asapPeer!!.sendASAPMessage(
                    "shark/messenger",
                    uri,
                    InMemoSharkNetMessage.serializeMessage(
                        contentType,
                        content,
                        this.asapPeer!!.peerID,
                        selectedRecipients,
                        sign,
                        encrypt,
                        this.sharkPKIComponent.asapKeyStore
                    )
                )
            }
        } catch (e: ASAPException) {
            throw SharkNetMessengerException(
                "when serialising and sending message: " + e.localizedMessage,
                e
            )
        }
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    override fun createClosedChannel(
        uri: CharSequence?,
        name: CharSequence?
    ): SharkNetMessengerClosedChannel? {
        this.checkComponentRunning()
        throw SharkNotSupportedException("not yet implemented")
    }

    @Throws(SharkNetMessengerException::class, IOException::class)
    override fun getChannel(uri: CharSequence?): SharkNetMessengerChannel {
        try {
            val asapStorage = this.asapPeer!!.getASAPStorage("shark/messenger")
            val channel = asapStorage.getChannel(uri)
            return SharkNetMessengerChannelImpl(this.asapPeer, this.sharkPKIComponent, channel)
        } catch (asapException: ASAPException) {
            throw SharkNetMessengerException(asapException)
        }
    }

    @Throws(SharkNetMessengerException::class, IOException::class)
    override fun getChannel(position: Int): SharkNetMessengerChannel {
        try {
            val uri = this.channelUris!![position]
            return this.getChannel(uri)
        } catch (var3: IndexOutOfBoundsException) {
            throw SharkNetMessengerException("channel position is out of bound: $position")
        }
    }

    @Throws(SharkNetMessengerException::class, IOException::class)
    override fun createChannel(uri: CharSequence?, name: CharSequence?): SharkNetMessengerChannel {
        return this.createChannel(uri, name, true)
    }

    @Throws(SharkNetMessengerException::class, IOException::class)
    override fun createChannel(
        uri: CharSequence?,
        name: CharSequence?,
        mustNotAlreadyExist: Boolean
    ): SharkNetMessengerChannel {
        this.checkComponentRunning()

        try {
            this.getChannel(uri)
            if (mustNotAlreadyExist) {
                throw SharkNetMessengerException("channel already exists")
            }
        } catch (var7: SharkNetMessengerException) {
        }

        try {
            val asapStorage = this.asapPeer!!.getASAPStorage("shark/messenger")
            asapStorage.createChannel(uri)
            val channel = asapStorage.getChannel(uri)
            return SharkNetMessengerChannelImpl(
                this.asapPeer,
                this.sharkPKIComponent,
                channel,
                name
            )
        } catch (asapException: ASAPException) {
            throw SharkNetMessengerException(asapException)
        }
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    override fun getChannelUris(): MutableList<CharSequence?>? {
        try {
            return this.asapPeer!!.getASAPStorage("shark/messenger").getChannelURIs()
        } catch (asapException: ASAPException) {
            throw SharkNetMessengerException(asapException)
        }
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    override fun removeChannel(uri: CharSequence?) {
        try {
            this.asapPeer!!.getASAPStorage("shark/messenger").removeChannel(uri)
        } catch (asapException: ASAPException) {
            throw SharkNetMessengerException(asapException)
        }
    }

    @Throws(IOException::class, SharkNetMessengerException::class)
    fun size(): Int {
        try {
            val asapStorage = this.asapPeer!!.getASAPStorage("shark/messenger")
            return asapStorage.getChannelURIs().size
        } catch (asapException: ASAPException) {
            throw SharkNetMessengerException(asapException)
        }
    }

    override fun getSharkPKI(): SharkPKIComponent {
        return this.sharkPKIComponent
    }

    override fun asapMessagesReceived(
        asapMessages: ASAPMessages,
        senderE2E: String?,
        asapHops: MutableList<ASAPHop?>?
    ) {
        val uri = asapMessages.uri
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)")
        this.notifySharkMessageReceivedListener(uri)
    }

    @get:Throws(IOException::class, ASAPException::class)
    val aSAPStorage: ASAPStorage
        get() = this.asapPeer!!.getASAPStorage("shark/messenger")
}
