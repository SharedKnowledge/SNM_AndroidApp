package net.sharksystem.sharknetmessengerandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import net.sharksystem.sharknetmessengerandroid.helper.Networking
import net.sharksystem.sharknetmessengerandroid.helper.SharkInit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SharkNetMessengerAndroidTheme {
                val ipAddress = Networking().getLocalIpAddress(this)  // Holt die IP-Adresse
                val test = SharkInit().printMessage(this)
                ExampleText(ipAddress,test)
            }
        }
    }
}

@Composable
fun ExampleText(ipAddress: String, test: String) {
    Box(
        modifier = Modifier.fillMaxSize()  // Füllt den gesamten Bildschirm
    ) {
        Text(
            text = "Hallo Welt!",
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = test,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = ipAddress,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Center)  // Positioniert den Text in der Mitte
        )

        Text(
            text = "Noch eine Position",
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.BottomCenter)  // Positioniert den Text unten rechts
        )
    }
}