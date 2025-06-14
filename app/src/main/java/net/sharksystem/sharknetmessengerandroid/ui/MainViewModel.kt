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

package net.sharksystem.sharknetmessengerandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.sharknetmessengerandroid.ui.conversation.SharkConversationUiState

/**
 * Used to communicate between screens.
 */
// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel
class MainViewModel : ViewModel() {
    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

    private val _channels = MutableStateFlow<List<String>>(emptyList())
    val channels: StateFlow<List<String>> = _channels.asStateFlow()

    fun loadChannels() {
        val loadedChannels: List<String> = SharkNetApp.getMessengerComponent()?.channelUris
            ?.map { it.toString() } ?: emptyList()
        _channels.value = loadedChannels
    }

    private val _currentChannelUri = MutableStateFlow("sn://universal")
    val currentChannelUri: StateFlow<String> = _currentChannelUri.asStateFlow()

    fun setCurrentChannelUri(uri: String) {
        _currentChannelUri.value = uri
    }

    val sharkUiState: StateFlow<SharkConversationUiState> =
        currentChannelUri.map { uri ->
            SharkConversationUiState(channelUri = uri) //on change create new uistate
        }.stateIn(
            scope = viewModelScope, //flow lives only in viewModel
            started = SharingStarted.WhileSubscribed(5000), //stops after 5 seconds of nobody listening
            initialValue = SharkConversationUiState(channelUri = _currentChannelUri.value) //sets first value to _currentChannelUri.value -> first value to sn://universal
        )

    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }
}
