package net.sharksystem.sharknetmessengerandroid.sharknet

import android.util.Log
import net.sharksystem.app.messenger.SharkNetMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SharkDataHelper {
    companion object {
        fun reloadMessages(uri: CharSequence): List<SharkNetMessage> {
            val messages = mutableListOf<SharkNetMessage>()
            val sharkNetMessages = SharkNetApp.getMessengerComponent()?.getChannel(uri)?.messages

            val size = sharkNetMessages!!.size()
            for (i in 0 until size) {
                val message = sharkNetMessages.getSharkMessage(i, true)
                messages.add(message)
            }
            Log.d("SharkDebug", "SharkNet messages reloaded")
            //@todo sharknetmessages reversed already integrated
            return messages.reversed()
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