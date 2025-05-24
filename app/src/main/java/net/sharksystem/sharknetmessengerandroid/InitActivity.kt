package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.pki.AndroidSharkPKIComponentImpl
import net.sharksystem.pki.SharkPKIComponent
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

class InitActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_peer) // load xml
        val peerName = findViewById<EditText>(R.id.PeerNameInput) // assign
        val button = findViewById<Button>(R.id.buttonSubmit)

        button.setOnClickListener {
            SharkNetApp.initialize(this, peerName.text.toString())
            val testPeerNameOutput = SharkNetApp.Companion.singleton?.getPeer()?.sharkPeerName

            //////// PKI Component ////////
            val pkiComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkPKIComponent::class.java)
            val pkiComponentImpl = pkiComponent as? AndroidSharkPKIComponentImpl

            val personvalueNumber = pkiComponentImpl?.numberOfPersons
            val certs = pkiComponentImpl?.certificates

            //////// Messanger Component ////////
            val messengerComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
            val messengerComponentImpl = messengerComponent as? SharkNetMessengerComponentImpl

            // Message list
            var channel_messages = messengerComponentImpl?.getChannel("sn://universal")?.messages

            // testMessage
            messengerComponentImpl?.sendSharkMessage(
                "text/plain",
                "Hallo ${SharkNetApp.Companion.singleton!!.getPeer().sharkPeerName}, deine Welt".toByteArray(),
                "sn://universal",
                true
            )

            var channelmnessagessize = channel_messages?.size()
            var testMessage = channel_messages?.getSharkMessage(0,false) // latest first

            // convert message content
            val byteArray: ByteArray = testMessage?.content ?: ByteArray(0)
            val messageString = byteArray.toString(Charsets.UTF_8)

            // convert message time
            val instant = Instant.ofEpochMilli(testMessage!!.creationTime)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val formattedTime = formatter.format(instant)
            val instant_key_creation = Instant.ofEpochMilli(pkiComponentImpl!!.keysCreationTime)
            val formatter_key_creation = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val formattedTime_key_creation = formatter.format(instant_key_creation)

            var finalString =
                    "Allgemein" +
                    "\nGesamtmessages: " + channelmnessagessize +
                    "\nChannels: " + messengerComponentImpl?.channelUris +
                    "\nASAP Storage: " + messengerComponentImpl?.asapStorage +
                    "\n\nMessage" +
                    "\nAbsender: " + testMessage.sender.toString() +
                    "\nCreation Time: " + formattedTime +
                    "\nRecipients: " + testMessage.recipients +
                    "\nContent Type: " + testMessage.contentType +
                    "\nContent: " + messageString +
                    "\nVerified: " + testMessage.verified() +
                    "\nSigned: " + testMessage.signed() +
                    "\nEncrypted: " + testMessage.encrypted() +
                    "\nCould be Decrypted: " + testMessage.couldBeDecrypted() +
                    "\n\nPKI" +
                    "\nPerson Value: " + personvalueNumber +
                    "\nCerts: " + certs +
                    "\nKey Creation Time: " + formattedTime_key_creation +
                    "\nPub Key Algorithm: " + pkiComponentImpl.publicKey.algorithm
            val textView = findViewById<TextView>(R.id.textView3)
            textView.text = finalString

            Toast.makeText(this, "Test Run läuft durch: $testPeerNameOutput", Toast.LENGTH_LONG).show()
            //Toast.makeText(this, "Dein Result: ${testString} ${channeltest}", Toast.LENGTH_LONG).show()
        }
    }
}