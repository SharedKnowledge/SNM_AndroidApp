package net.sharksystem.sharknetmessengerandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import androidx.core.content.edit
import net.sharksystem.sharknetmessengerandroid.MainActivity
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SharkNetMessengerAndroidTheme() {}
            OnboardingScreenComposable()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveUsername(username: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit {
            putString("user_name", username)
            putBoolean("peer_name_set", true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun OnboardingScreenComposable() {
        var username by remember { mutableStateOf("") }
        val context = androidx.compose.ui.platform.LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome! Please enter your username.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.trim() },
                label = { Text("Username") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val finalUsername = username.trim()
                if (finalUsername.isEmpty()) {
                    Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                } else {
                    saveUsername(finalUsername)
                    SharkNetApp.initialize(context,finalUsername)
                    Toast.makeText(context,"Peer with name ${finalUsername} was created", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            }) {
                Text("Continue")
            }
        }
    }

    @Preview
    @Composable
    fun OnboardingScreenPreview() {
        SharkNetMessengerAndroidTheme {
            OnboardingScreenComposable()
        }
    }
}