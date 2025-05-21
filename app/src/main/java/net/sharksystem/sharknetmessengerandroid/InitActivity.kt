package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

class InitActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_peer) // load xml
        val peerName = findViewById<EditText>(R.id.PeerNameInput) // assign
        val button = findViewById<Button>(R.id.buttonSubmit)

        button.setOnClickListener {
            SharkNetApp.initialize(this, peerName.text.toString())
            val testString = SharkNetApp.Companion.singleton?.getPeer()?.sharkPeerName

            val messengerComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
            val messengerComponentImpl = messengerComponent as? SharkNetMessengerComponentImpl
            messengerComponentImpl?.sendSharkMessage(
                "text/plain",
                "Hallo deine Welt".toByteArray(),
                "sn://universal",
                true
            )
            //var channeltest = messengerComponentImpl?.getChannel(0)?.messages?.getSharkMessage(0,true)?.content.toString()
            //var channeltest = messengerComponentImpl?.getChannel(0)?.messages?.getSharkMessage(0,true)?.contentType
            var channelmnessagessize = messengerComponentImpl?.getChannel(0)?.messages?.size()
            var channeltest = messengerComponentImpl?.getChannel(0)?.messages?.getSharkMessage(0,false)?.content
            val byteArray: ByteArray = channeltest ?: ByteArray(0)
            val resultString = byteArray.toString(Charsets.UTF_8)
            Toast.makeText(this, resultString, Toast.LENGTH_LONG).show()
            //Toast.makeText(this, "Dein Result: ${testString} ${channeltest}", Toast.LENGTH_LONG).show()
        }
    }
}