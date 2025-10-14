/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified from the Official Jetpack Compose samples, provided by the Android Open Source Project
// Original source: https://github.com/android/compose-samples
// Changes:

package net.sharksystem.sharknetmessengerandroid.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.sharksystem.sharknetmessengerandroid.R
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
//import com.example.compose.jetchat.widget.WidgetReceiver
import net.sharksystem.sharknetmessengerandroid.ui.data.sharkColleagueProfile
import net.sharksystem.sharknetmessengerandroid.ui.data.sharkMeProfile
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme


//copy profile to shark

@Composable
fun DrawerContent(
    channels: List<String>,
    onProfileClicked: (String) -> Unit,
    onChatClicked: (String) -> Unit,
    selectedMenu: String = "composers"
) {

    // Use windowInsetsTopHeight() to add a spacer which pushes the drawer content
    // below the status bar (y-axis)
    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DrawerHeader()
        DividerItem()
        //DividerItem(modifier = Modifier.padding(horizontal = 28.dp))
        //DrawerItemHeader("Settings")
        CertificatesItem()
        PersonsItem()
        ConnectionItem()
        DividerItem(modifier = Modifier.padding(horizontal = 28.dp))
        DrawerItemHeader("Channels")
//        SharkNetApp.getMessengerComponent()?.channelUris?.forEach { channelUri ->
//            val channelName = channelUri.toString()
//            ChatItem(channelName, selectedMenu == channelUri.toString()) {
//                onChatClicked(channelUri.toString())
//            }
//        }
        channels.forEach { channelUri ->
            ChatItem(channelUri, selectedMenu == channelUri) {
                onChatClicked(channelUri)
            }
        }
        DividerItem(modifier = Modifier.padding(horizontal = 28.dp))
        /*
        DrawerItemHeader("Persons")
        ProfileItem(
            "Ali Conors (you)", sharkMeProfile.photo,
            selectedMenu == sharkMeProfile.userId
        ) {
            onProfileClicked(sharkMeProfile.userId)
        }
        ProfileItem(
            "Taylor Brooks", sharkColleagueProfile.photo,
            selectedMenu == sharkColleagueProfile.userId
        ) {
            onProfileClicked(sharkColleagueProfile.userId)
        }
         */
    }
}

@Composable
private fun DrawerHeader() {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = CenterVertically) {
        SharkIcon(
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.shark_icon),
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DrawerItemHeader(text: String) {
    Box(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .padding(horizontal = 28.dp),
        contentAlignment = CenterStart
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatItem(text: String, selected: Boolean, onChatClicked: () -> Unit) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = onChatClicked),
        verticalAlignment = CenterVertically
    ) {
        val iconTint = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(
            painter = painterResource(id = R.drawable.shark_icon),
            tint = iconTint,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = null
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun ProfileItem(
    text: String,
    @DrawableRes profilePic: Int?,
    selected: Boolean = false,
    onProfileClicked: () -> Unit
) {
    val background = if (selected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .then(background)
            .clickable(onClick = onProfileClicked),
        verticalAlignment = CenterVertically
    ) {
        val paddingSizeModifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            .size(24.dp)
        if (profilePic != null) {
            Image(
                painter = painterResource(id = profilePic),
                modifier = paddingSizeModifier.then(Modifier.clip(CircleShape)),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        } else {
            Spacer(modifier = paddingSizeModifier)
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun DividerItem(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
@Preview
fun DrawerPreview() {
    SharkNetMessengerAndroidTheme {
        Surface {
            Column {
                //DrawerContent({}, {})
            }
        }
    }
}

@Composable
@Preview
fun DrawerPreviewDark() {
    SharkNetMessengerAndroidTheme(darkTheme = true) {
        Surface {
            Column {
                //DrawerContent({}, {})
            }
        }
    }
}


@Composable
private fun CertificatesItem() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .clickable(onClick = {
                Log.d("SharkDebug", "Einstellungen geöffnet.")
                val intent = Intent(context, net.sharksystem.sharknetmessengerandroid.ui.settings.SettingsActivity::class.java)
                context.startActivity(intent)
            }),
        verticalAlignment = CenterVertically
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_preferences),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = "Certificates"
        )
        Text(
            text = "Certificates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun PersonsItem() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .clickable(onClick = {
                Log.d("SharkDebug", "Einstellungen geöffnet.")
                val intent = Intent(context, net.sharksystem.sharknetmessengerandroid.ui.settings.SettingsActivity::class.java)
                context.startActivity(intent)
            }),
        verticalAlignment = CenterVertically
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_view),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = "Persons"
        )
        Text(
            text = "Persons",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
@Composable
private fun ConnectionItem() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .clickable(onClick = {
                Log.d("SharkDebug", "Einstellungen geöffnet.")
                val intent = Intent(context, net.sharksystem.sharknetmessengerandroid.ui.settings.SettingsActivity::class.java)
                context.startActivity(intent)
            }),
        verticalAlignment = CenterVertically
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_view),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            contentDescription = "Connection"
        )
        Text(
            text = "Connection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}