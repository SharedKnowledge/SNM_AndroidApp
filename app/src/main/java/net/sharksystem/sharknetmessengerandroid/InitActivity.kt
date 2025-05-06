package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

class InitActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_activity)
        var sharkNetApp: SharkNetApp
        val peerName = findViewById<EditText>(R.id.PeerNameInput)
        val button = findViewById<Button>(R.id.buttonSubmit)

        button.setOnClickListener {
            SharkNetApp.initialize(this, peerName.text.toString())
            val testString = SharkNetApp.Companion.singleton?.getPeer()?.sharkPeerName
            Toast.makeText(this, "Dein Result: ${testString} ", Toast.LENGTH_SHORT).show()
        }
    }
}