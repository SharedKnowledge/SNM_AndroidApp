package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

class NetworkSettingsActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_settings_activity)
        //var shark_peer: SHARK
        var sharkNetApp: SharkNetApp
        val peerName = findViewById<EditText>(R.id.PeerNameInput)
        val portInput = findViewById<EditText>(R.id.PortInput)
        val button = findViewById<Button>(R.id.buttonSubmit)

        button.setOnClickListener {
            sharkNetApp = SharkNetApp(this,peerName.text.toString())
            //shark_peer = SHARK("TestPeer")
            //shark_peer.openTCP(portInput.text.toString().toInt())
            SharkNetApp.initialize(this,peerName.text.toString())
            val testString = SharkNetApp.singleton.getPeer().sharkPeerName
            Toast.makeText(this, "Dein Result: ${testString} ", Toast.LENGTH_SHORT).show()
        }
    }
}