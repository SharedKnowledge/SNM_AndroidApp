package net.sharksystem.sharknetmessengerandroid.helper

import android.content.Context
import net.sharksystem.pki.PKIHelper.sixDigitsToString

class SharkInit {
    companion object {
        val test: String = sixDigitsToString(123456)
        const val SETTINGSFILENAME: String = ".sharkNetMessengerSessionSettings"
        const val PEERNAME_KEY: String = "peername"
        const val SYNC_WITH_OTHERS_IN_SECONDS_KEY: String = "syncWithOthersInSeconds"
    }
    fun printMessage(context: Context) : String {
        print("testtesttest")
        return sixDigitsToString(123456)
    }
}