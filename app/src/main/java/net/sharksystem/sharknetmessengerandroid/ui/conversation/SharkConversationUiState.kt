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

package net.sharksystem.sharknetmessengerandroid.ui.conversation

import androidx.compose.runtime.toMutableStateList
import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.sharknetmessengerandroid.ui.data.SharkData

class SharkConversationUiState(
    val channelUri: String,
    initialMessages: List<SharkNetMessage>
) {
    //private val initialMessages: List<SharkNetMessage> = SharkData.reloadMessages(channelUri)
    private val _messages: MutableList<SharkNetMessage> = initialMessages.toMutableStateList()
    val messages: List<SharkNetMessage> = _messages

    fun addMessage(msg: String, msgType: String) {
        val messengerComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
        val messengerComponentImpl = messengerComponent as? SharkNetMessengerComponentImpl
        messengerComponentImpl?.sendSharkMessage(
            msgType, //"text/plain"
            msg.toByteArray(),
            this.channelUri,
            true
        )
        val newMessages: List<SharkNetMessage> = SharkData.reloadMessages(channelUri)
        _messages.addAll(newMessages)
    }
}
