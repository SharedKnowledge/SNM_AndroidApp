package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InitActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_peer) // load xml
        val peerName = findViewById<EditText>(R.id.PeerNameInput) // assign
        val button = findViewById<Button>(R.id.buttonSubmit)
        val textview = findViewById<TextView>(R.id.textView3)

        button.setOnClickListener {
            SharkNetApp.initialize(this, peerName.text.toString())
            val testString = SharkNetApp.Companion.singleton?.getPeer()?.sharkPeerName

            val messengerComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
            val messengerComponentImpl = messengerComponent as? SharkNetMessengerComponentImpl
            messengerComponentImpl?.sendSharkMessage(
                "text/plain",
                "Hallo ${SharkNetApp.Companion.singleton!!.getPeer().sharkPeerName}, deine Welt".toByteArray(),
                "sn://universal",
                true
            )
            //var channeltest = messengerComponentImpl?.getChannel(0)?.messages?.getSharkMessage(0,true)?.content.toString()
            //var channeltest = messengerComponentImpl?.getChannel(0)?.messages?.getSharkMessage(0,true)?.contentType
            var channelmnessagessize = messengerComponentImpl?.getChannel(0)?.messages?.size()
            var testMessage = messengerComponentImpl?.getChannel("sn://universal")?.messages?.getSharkMessage(0,false)

            // convert message content
            val byteArray: ByteArray = testMessage?.content ?: ByteArray(0)
            val messageString = byteArray.toString(Charsets.UTF_8)

            // convert message time
            val instant = Instant.ofEpochMilli(testMessage!!.creationTime)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())

            val formattedTime = formatter.format(instant)

            var finalString = "Gesamtmessages: " + channelmnessagessize +
                    "\nAbsender: " + testMessage.sender.toString() +
                    "\nCreation Time: " + formattedTime +
                    "\nRecipients: " + testMessage.recipients +
                    "\nContent Type: " + testMessage.contentType +
                    "\nContent: " + messageString +
                    "\nVerified: " + testMessage.verified() +
                    "\nSigned: " + testMessage.signed() +
                    "\nEncrypted: " + testMessage.encrypted() +
                    "\nCould be Decrypted: " + testMessage.couldBeDecrypted()
            textview.text = finalString
            Toast.makeText(this, "Test Run läuft durch", Toast.LENGTH_LONG).show()
            //Toast.makeText(this, "Dein Result: ${testString} ${channeltest}", Toast.LENGTH_LONG).show()
        }
    }
}