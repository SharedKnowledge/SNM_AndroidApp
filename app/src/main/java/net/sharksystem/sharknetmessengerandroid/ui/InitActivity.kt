package net.sharksystem.sharknetmessengerandroid.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import net.sharksystem.sharknetmessengerandroid.R
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

class InitActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_activity)
        var sharkNetApp: SharkNetApp
        val peerName = findViewById<EditText>(R.id.PeerNameInput)
        val button = findViewById<Button>(R.id.buttonSubmit)

        button.setOnClickListener {
            sharkNetApp = SharkNetApp(this, peerName.text.toString())
            SharkNetApp.Companion.initialize(this,peerName.text.toString())
            val testString = SharkNetApp.Companion.singleton
            Toast.makeText(this, "Dein Result: ${testString?.getPeer()?.sharkPeerName} ", Toast.LENGTH_SHORT).show()
        }
    }
}