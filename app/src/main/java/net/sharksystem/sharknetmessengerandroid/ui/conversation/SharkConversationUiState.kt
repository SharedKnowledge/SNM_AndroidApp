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
//@todo @Monty pls add changes to comment if needed

package net.sharksystem.sharknetmessengerandroid.ui.conversation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.toMutableStateList
import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessengerComponent
import net.sharksystem.app.messenger.SharkNetMessengerComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.sharknetmessengerandroid.ui.data.SharkDataHelper
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms
import net.sharksystem.sharknetmessengerandroid.sharknet.AndroidASAPKeyStoreNew
import net.sharksystem.asap.utils.ASAPSerialization
import java.security.KeyPair

/**
 * Represents the UI state of a SharkNet conversation.
 */
class SharkConversationUiState(
    val channelUri: String,
    initialMessages: List<SharkNetMessage>
) {
    constructor(channelUri: String) : this(
        channelUri,
        SharkDataHelper.reloadMessages(channelUri)
    )
    //private val initialMessages: List<SharkNetMessage> = SharkData.reloadMessages(channelUri)
    private val _messages: MutableList<SharkNetMessage> = initialMessages.toMutableStateList()
    val messages: List<SharkNetMessage> = _messages

    /**
     * Adds a message to the conversation.
     */
    fun addMessage(
        context: Context,
        msg: String,
        msgType: String,
        signed: Boolean,
        encrypted: Boolean,
        selectedRecipients: MutableSet<CharSequence>? = mutableSetOf(),
        selectedFileUri: Uri? = null
    ) {
        if (msg.isEmpty() && selectedFileUri == null)
            return

        val messengerComponent = SharkNetApp.Companion.singleton?.getPeer()?.getComponent(SharkNetMessengerComponent::class.java)
        val messengerComponentImpl = messengerComponent as? SharkNetMessengerComponentImpl

        val fileContent: ByteArray? = selectedFileUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }

        val contentToSend: ByteArray = fileContent ?: msg.toByteArray()
        var finalContent: ByteArray = contentToSend

        if (encrypted && !selectedRecipients.isNullOrEmpty()) {
                if (msgType == ContentDescriptors.CHAR.toString()) {
                    try{
                    val keyStore = AndroidASAPKeyStoreNew(
                    context,
                    SharkNetApp.singleton?.getPeer()?.peerID.toString()
                )
                ASAPCryptoAlgorithms.produceEncryptedMessagePackage(
                    contentToSend,
                    selectedRecipients as CharSequence?,
                    keyStore
                ) ?: throw Exception("encryption failed")
            } catch (e: Exception) {
                Toast.makeText(context, "Encryption failed: ${e.message}", Toast.LENGTH_LONG).show()
                        return
                }
            }
        }
        //key pair must be generated before signing
//        if (signed) {
//            try {
//                val keyStore = AndroidASAPKeyStoreNew(
//                    context,
//                    SharkNetApp.singleton?.getPeer()?.peerID.toString()
//                )
//                finalContent = ASAPCryptoAlgorithms.sign(
//                    contentToSend,
//                    keyStore
//                ) ?: throw Exception("Signing failed")
//            } catch (e: Exception) {
//                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
//                return
//            }
//        }
        messengerComponentImpl?.sendSharkMessage(
            msgType,
            finalContent,
            this.channelUri,
            selectedRecipients,
            signed,
            encrypted
        )

        val newMessages: List<SharkNetMessage> = SharkDataHelper.reloadMessages(channelUri)
        _messages.clear()
        _messages.addAll(newMessages)
    }
}