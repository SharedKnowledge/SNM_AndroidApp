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

@file:OptIn(ExperimentalMaterial3Api::class)

package net.sharksystem.sharknetmessengerandroid.ui.conversation

import FunctionalityNotAvailablePopup
import android.R.attr.onClick
import android.content.ClipDescription
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.sharksystem.app.messenger.SharkNetMessage
import net.sharksystem.app.messenger.SharkNetMessengerChannel
import net.sharksystem.app.messenger.SharkNetMessengerChannelImpl
import net.sharksystem.sharknetmessengerandroid.R
import net.sharksystem.sharknetmessengerandroid.ui.components.AppBar
import net.sharksystem.sharknetmessengerandroid.ui.data.SharkDataHelper
import net.sharksystem.sharknetmessengerandroid.ui.data.sharkExampleUiState
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.compareTo

/**
 * Entry point for a conversation screen.
 *
 * @param uiState [ConversationUiState] that contains messages to display
 * @param navigateToProfile User action when navigation to a profile is requested
 * @param modifier [Modifier] to apply to this layout node
 * @param onNavIconPressed Sends an event up when the user clicks on the menu
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SharkConversationContent(
    uiState: SharkConversationUiState,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { }
) {

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    var background by remember {
        mutableStateOf(Color.Transparent)
    }

    var borderStroke by remember {
        mutableStateOf(Color.Transparent)
    }

    var encrypted by remember { mutableStateOf(false) }
    var signed by remember { mutableStateOf(false) }
    var selectedRecipients by remember { mutableStateOf<MutableSet<CharSequence>>(mutableSetOf()) }

    val dragAndDropCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val clipData = event.toAndroidDragEvent().clipData

                if (clipData.itemCount < 1) {
                    return false
                }

                uiState.addMessage(
                    clipData.getItemAt(0).text.toString(),"text/plain",signed,encrypted,selectedRecipients
                )

                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                borderStroke = Color.Red
            }

            override fun onEntered(event: DragAndDropEvent) {
                super.onEntered(event)
                background = Color.Red.copy(alpha = .3f)
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                background = Color.Transparent
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                background = Color.Transparent
                borderStroke = Color.Transparent
            }
        }
    }




    Scaffold(
        topBar = {
            SharkChannelNameBar(
                channelUri = uiState.channelUri,
                onNavIconPressed = onNavIconPressed,
                scrollBehavior = scrollBehavior,
            )
        },
        // Exclude ime and navigation bar padding so this can be added by the UserInput composable
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = background)
                .border(width = 2.dp, color = borderStroke)
                .dragAndDropTarget(shouldStartDragAndDrop = { event ->
                    event
                        .mimeTypes()
                        .contains(
                            ClipDescription.MIMETYPE_TEXT_PLAIN
                        )
                }, target = dragAndDropCallback)
        ) {
            SNMessages(
                messages = uiState.messages,
                navigateToProfile = navigateToProfile,
                modifier = Modifier.weight(1f),
                scrollState = scrollState
            )
            UserInput(
                onMessageSent = { content ->
                    uiState.addMessage(
                        content,"text/plain",signed,encrypted,selectedRecipients
                    )
                },
                resetScroll = {
                    scope.launch {
                        scrollState.scrollToItem(0)
                    }
                },
                // let this element handle the padding so that the elevation is shown behind the
                // navigation bar
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharkChannelNameBar(
    channelUri: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { }
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    var showChannelDetails by remember { mutableStateOf(false) }

    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }
//    if (showChannelDetails) {
//        ChannelDetailsModal { showChannelDetails = false }
//    }
    AppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Channel name
                Text(
                    text = channelUri,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        actions = {
            // Search icon
            Icon(
                imageVector = Icons.Outlined.Search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = { functionalityNotAvailablePopupShown = true })
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .height(24.dp),
                contentDescription = stringResource(id = R.string.search)
            )
            // Info icon
            Icon(
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = { functionalityNotAvailablePopupShown = true })
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .height(24.dp),
                contentDescription = stringResource(id = R.string.info)
            )
            // Messages Refresh Icon
            Icon(
                imageVector = Icons.Outlined.Refresh,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = { SharkDataHelper.reloadMessages(channelUri) })
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .height(24.dp),
                contentDescription = stringResource(id = R.string.info) //@todo fix string
            )
        }
    )
}

const val SNConversationTestTag = "ConversationTestTag"

@Composable
fun SNMessages(
    messages: List<SharkNetMessage>,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {

        val authorMe = stringResource(id = R.string.author_me)
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
                .testTag(SNConversationTestTag)
                .fillMaxSize()
        ) {
            for (index in messages.indices) {
                val content = messages[index]

                var formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
                val currentMsgDate = SharkDataHelper.getDate(messages[index].creationTime).format(formatter)
                val prevMsgDate = messages.getOrNull(index + 1)?.let {
                    SharkDataHelper.getDate(it.creationTime).format(formatter)
                }

                item {
                    SNMessage(
                        onAuthorClick = { name -> navigateToProfile(name) },
                        msg = content,
                        isUserMe = content.sender == authorMe,
                    )
                }

                if (currentMsgDate != prevMsgDate) {
                    item {
                        SNDayHeader(currentMsgDate)
                    }
                }
            }
        }
        // Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Show the button if the first visible item is not the first one or if the offset is
        // greater than the threshold.
        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SNMessage(
    onAuthorClick: (String) -> Unit,
    msg: SharkNetMessage,
    isUserMe: Boolean,
) {

    val spaceBetweenAuthors = Modifier.padding(top = 8.dp)
    Row(modifier = spaceBetweenAuthors) {
        // Avatar
        Image(
            painter = painterResource(id = R.drawable.placeholder_avatar),
            //@todo try from shark data
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )
        SNAuthorAndTextMessage(
            msg = msg,
            isUserMe = isUserMe,
            authorClicked = onAuthorClick,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
fun SNAuthorAndTextMessage(
    msg: SharkNetMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SNAuthorNameTimestamp(msg)
        SNChatItemBubble(msg, isUserMe, authorClicked = authorClicked)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SNAuthorNameTimestamp(msg: SharkNetMessage) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = msg.sender.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = SharkDataHelper.formatTime(msg.creationTime),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignBy(LastBaseline),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

@Composable
fun SNDayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    HorizontalDivider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun SNChatItemBubble(
    message: SharkNetMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    var showMessageDetails by remember { mutableStateOf(false) }

    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    if (showMessageDetails) {
        MessageDetailModal(
            message = message,
            onDismiss = { showMessageDetails = false }
        )
    }

    val longPressModifier = Modifier.combinedClickable(
        onClick = {
//            Log.d("SharkDebug", "Short press detected")
        },
        onLongClick = {
            showMessageDetails = true
//            Log.d("SharkDebug", "Long press detected")
        }
    )

    Column {
        Surface(
            color = backgroundBubbleColor,
            shape = ChatBubbleShape,
            modifier = longPressModifier
        ) {
            SNClickableMessage(
                message = message,
                isUserMe = isUserMe,
                authorClicked = authorClicked
            )
        }
    }
}

@Composable
fun SNClickableMessage(
    message: SharkNetMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    var showMessageDetails by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = String(message.content, Charsets.UTF_8),
        primary = isUserMe
    )

    if (showMessageDetails) {
        MessageDetailModal(
            message = message,
            onDismiss = { showMessageDetails = false }
        )
    }

//    Text(
//        text = styledMessage,
//        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
//        modifier = Modifier.padding(16.dp)
//            .combinedClickable(
//                onClick = { styledMessage.getStringAnnotations(start = ) },
//                onLongClick = { }
//            )
//    )

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        modifier = Modifier.padding(16.dp),
        onClick = {
                val stringAnnotation = styledMessage
                    .getStringAnnotations(start = it, end = it)
                    .firstOrNull()
                if (stringAnnotation != null) {
                    stringAnnotation.let { annotation ->
                        when (annotation.tag) {
                            SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                            SymbolAnnotationType.PERSON.name -> authorClicked(annotation.item)
                            else -> Unit
                        }
                    }
                }
                else { showMessageDetails = true }
        }
    )
}

@Preview
@Composable
fun SNConversationPreview() {
    SharkNetMessengerAndroidTheme {
        SharkConversationContent(
            uiState = sharkExampleUiState,
            navigateToProfile = { }
        )
    }
}

@Preview
@Composable
fun SNChannelBarPrev() {
    SharkNetMessengerAndroidTheme {
        SharkChannelNameBar(channelUri = "sn://universal")
    }
}

@Preview
@Composable
fun SNDayHeaderPrev() {
    SNDayHeader("Aug 6")
}

private val JumpToBottomThreshold = 56.dp

/**
 * Zeigt detaillierte Informationen über eine Nachricht in einem Bottom Sheet Modal an
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailModal(
    message: SharkNetMessage,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nachrichtendetails",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Schließen"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MessageDetailCard(
                title = "Nachrichteninhalt",
                content = String(message.content, Charsets.UTF_8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zeit formatieren
            val instant = Instant.ofEpochMilli(message.creationTime)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val formattedTime = formatter.format(instant)

            MessageDetailCard(
                title = "Zeitstempel",
                content = formattedTime
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageDetailCard(
                title = "Absender",
                content = message.sender.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageDetailCard(
                title = "Empfänger",
                content = message.recipients.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageDetailCard(
                title = "Content Type",
                content = message.contentType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageDetailCard(
                title = "Sicherheitsinfo",
                content = "Verifiziert: ${message.verified()}\n" +
                        "Signiert: ${message.signed()}\n" +
                        "Verschlüsselt: ${message.encrypted()}\n" +
                        "Konnte entschlüsselt werden: ${message.couldBeDecrypted()}"
            )

            // Extra Platz am Ende hinzufügen
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

///**
// * Zeigt detaillierte Informationen über den Kanal in einem Bottom Sheet Modal an
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChannelDetailsModal(
//    channel: SharkNetMessengerChannelImpl,
//    onDismiss: () -> Unit
//) {
//    val modalBottomSheetState = rememberModalBottomSheetState()
//    val scrollState = rememberScrollState()
//
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = modalBottomSheetState
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .verticalScroll(scrollState)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Nachrichtendetails",
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.weight(1f)
//                )
//
//                IconButton(onClick = onDismiss) {
//                    Icon(
//                        imageVector = Icons.Outlined.Close,
//                        contentDescription = "Schließen"
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            MessageDetailCard(
//                title = "Nachrichteninhalt",
//                content = String(message.content, Charsets.UTF_8)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Zeit formatieren
//            val instant = Instant.ofEpochMilli(message.creationTime)
//            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
//                .withZone(ZoneId.systemDefault())
//            val formattedTime = formatter.format(instant)
//
//            MessageDetailCard(
//                title = "Zeitstempel",
//                content = formattedTime
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            MessageDetailCard(
//                title = "Absender",
//                content = message.sender.toString()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            MessageDetailCard(
//                title = "Empfänger",
//                content = message.recipients.toString()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            MessageDetailCard(
//                title = "Content Type",
//                content = message.contentType.toString()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            MessageDetailCard(
//                title = "Sicherheitsinfo",
//                content = "Verifiziert: ${message.verified()}\n" +
//                        "Signiert: ${message.signed()}\n" +
//                        "Verschlüsselt: ${message.encrypted()}\n" +
//                        "Konnte entschlüsselt werden: ${message.couldBeDecrypted()}"
//            )
//
//            // Extra Platz am Ende hinzufügen
//            Spacer(modifier = Modifier.height(32.dp))
//        }
//    }
//}

/**
 * Hilfsfunktion zum Anzeigen von Details in einem Kartenformat
 */
@Composable
fun MessageDetailCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
