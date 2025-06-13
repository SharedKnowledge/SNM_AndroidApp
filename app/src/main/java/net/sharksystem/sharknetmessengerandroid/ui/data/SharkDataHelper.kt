package net.sharksystem.sharknetmessengerandroid.ui.data

import android.util.Log
import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SharkDataHelper {
    companion object {
        fun reloadMessages(uri: CharSequence): List<SharkNetMessage> {
            val messages = mutableListOf<SharkNetMessage>()
            val sharkNetMessages = (SharkNetApp.Companion.singleton?.
            getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
                    as? SharkNetMessengerComponentImpl)?.
            getChannel(uri)?.messages

            val size = sharkNetMessages!!.size()
            for (i in 0 until size) {
                val message = sharkNetMessages.getSharkMessage(i, true)
                messages.add(message)
            }
            Log.d("SharkDebug", "SharkNet messages reloaded")

            return messages.reversed()
        }

        fun countMessages(uri: CharSequence): Int {
            val sharkNetMessages = (SharkNetApp.Companion.singleton?.
            getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
                    as? SharkNetMessengerComponentImpl)?.
            getChannel(uri)?.messages
            return sharkNetMessages!!.size()
        }

        fun transformToTime(timeInMillis: Long): ZonedDateTime {
            return Instant.ofEpochMilli(timeInMillis)
                .atZone(ZoneId.systemDefault())
        }

        fun formatTime(timeInMillis: Long): String {
            val dateTime = transformToTime(timeInMillis)

            val formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            return formatted
        }
        fun getDate(timeinMillis: Long): LocalDate {
            return transformToTime(timeinMillis).toLocalDate()
        }
    }
}