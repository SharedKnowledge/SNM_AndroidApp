package net.sharksystem.sharknetmessengerandroid.ui.pki

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.sharksystem.asap.persons.PersonValues
import net.sharksystem.sharknetmessengerandroid.ui.pki.certificates.CertificatesActivity
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PKIActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SharkNetMessengerAndroidTheme {
                PKIScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PKIScreen(
    onBackPressed: () -> Unit = {}
) {
    var settingsState by remember { mutableStateOf<PKIViewModel?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        settingsState = PKIViewModel()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Public Key Infrastructure") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        settingsState?.refresh()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                settingsState?.let { state ->
                    if (state.isLoading) {
                        LoadingSection()
                    } else if (state.hasError()) {
                        ErrorSection(state.errorMessage)
                    } else {
                        GeneralSection(state)

                        Spacer(modifier = Modifier.height(16.dp))

                        PeersSection(state)

                        Spacer(modifier = Modifier.height(16.dp))

                        CertificatesSection(state)
                    }
                } ?: run {
                    LoadingSection()
                }
            }
        }
    }
}

@Composable
fun LoadingSection() {
    PKICard(
        title = "Loading...",
        icon = Icons.Default.Refresh
    ) {
        Text(
            text = "Initializing PKI settings...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ErrorSection(errorMessage: String) {
    PKICard(
        title = "Error",
        icon = Icons.Default.Security
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun GeneralSection(state: PKIViewModel) {
    PKICard(
        title = "General",
        icon = Icons.Default.Security
    ) {
        PKIItem("Owner Name", state.ownerName)
        ClickablePKIValue("Owner ID", state.ownerID.take(23) + "...", state.ownerID)
        PKIItem("Known Persons", state.numberOfPersons.toString())
        PKIItem("Total Certificates", state.totalCertificates.toString())
        val keyAge = (System.currentTimeMillis() - state.keyCreationTime) / (1000 * 60 * 60 * 24)
        PKIItem("Key Age", "$keyAge days")

        Spacer(modifier = Modifier.height(16.dp))

        var showConfirmDialog by remember { mutableStateOf(false) }

        if (state.keyCreationTime > 0) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val keyDate = dateFormat.format(Date(state.keyCreationTime))
            PKIItem("Key Created", keyDate)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Key, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Generate New Key Pair")
        }

        Text(
            text = "Generating a new key pair will invalidate all existing certificates",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "Generate New Key Pair?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "This action will:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Invalidate all existing certificates",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Require re-establishing trust with all peers",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Cannot be undone",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Are you sure you want to continue?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            state.generateNewKeyPair()
                        }
                    ) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { state.sendCredentialMessage() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Send Credential Message to all Peers")
        }

        Text(
            text = "Sends your credentials to all currently connected peers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PeersSection(state: PKIViewModel) {
    var peersToShow by remember { mutableIntStateOf(5) }
/*
    PKICard(
        title = "Peers",
        icon = Icons.Default.People
    ) {
        if (state.knownPersons.isEmpty()) {
            Text(
                text = "No known persons yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Known Persons (${state.knownPersons.size})",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.knownPersons.take(peersToShow).forEach { person ->
                val personName = person.name?.toString() ?: "Unknown"
                val personId = person.userID?.toString() ?: "Unknown"
                val identityAssurance = state.getIdentityAssurance(personId)
                val trustLevel = when {
                    identityAssurance <= 2 -> "High Trust"
                    identityAssurance <= 5 -> "Medium Trust"
                    identityAssurance <= 8 -> "Low Trust"
                    else -> "Not trusted"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = personName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = trustLevel,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                identityAssurance <= 2 -> MaterialTheme.colorScheme.primary
                                identityAssurance <= 5 -> MaterialTheme.colorScheme.tertiary
                                identityAssurance <= 8 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    Text(
                        text = "ID: ${if (personId.length > 25) personId.take(25) + "..." else personId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Load more button
            if (state.knownPersons.size > peersToShow) {
                val remainingPeers = state.knownPersons.size - peersToShow

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        peersToShow = minOf(peersToShow + 5, state.knownPersons.size)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show $remainingPeers more peer${if (remainingPeers == 1) "" else "s"}")
                }
            }

            // Show less button
            if (peersToShow > 5 && state.knownPersons.size > 5) {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { peersToShow = 5 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show less")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!state.knownPersons.isEmpty()) {
            Text(
                text = "Global Signing Failure Rate",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Set signing failure rate for all known peers (10% - 100%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            var globalFailureRate by remember { mutableFloatStateOf(state.getInitialGlobalSigningFailureRate().toFloat()) }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = globalFailureRate,
                    onValueChange = { globalFailureRate = it },
                    valueRange = 1f..10f,
                    steps = 8, // 1-10 with step of 1 (8 steps between 1 and 10)
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    onValueChangeFinished = {
                        state.setAllPeersSigningFailureRate(globalFailureRate.toInt())
                    }
                )

                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Current: ${globalFailureRate.toInt()}/10 (${((globalFailureRate / 10) * 100).toInt()}% failure rate)",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    globalFailureRate <= 3f -> MaterialTheme.colorScheme.primary
                    globalFailureRate <= 7f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }*/
}

@Preview
@Composable
fun PeersSectionPreview() {
    SharkNetMessengerAndroidTheme {
        val mockState = object : PKIViewModel() {
            override fun getInitialGlobalSigningFailureRate(): Int = 3

            override fun getIdentityAssurance(personID: String?): Int {
                return when {
                    personID?.startsWith("alice") == true -> 1
                    personID?.startsWith("bob") == true -> 3
                    personID?.startsWith("charlie") == true -> 6
                    personID?.startsWith("diana") == true -> 2
                    personID?.startsWith("eve") == true -> 9
                    else -> 5
                }
            }

            init {
                // Override the known persons list for preview
                knownPersons = mutableListOf(
                    createMockPerson("Alice", "alice123456789"),
                    createMockPerson("Bob", "bob987654321"),
                    createMockPerson("Charlie", "charlie456789123"),
                    createMockPerson("Diana", "diana789123456"),
                    createMockPerson("Eve", "eve321654987"),
                    createMockPerson("Frank", "frank654987321"),
                    createMockPerson("Grace", "grace159753486")
                )
            }

            private fun createMockPerson(name: String, id: String): PersonValues {
                return object : PersonValues {
                    private var personName: CharSequence = name
                    private var signingFailureRate: Int = 5

                    override fun getName(): CharSequence = personName
                    override fun getUserID(): CharSequence = id

                    override fun setName(name: CharSequence) {
                        personName = name
                    }

                    override fun getSigningFailureRate(): Int = signingFailureRate

                    override fun setSigningFailureRate(failureRate: Int) {
                        signingFailureRate = failureRate
                    }
                }
            }
        }
        PeersSection(mockState)
    }
}

@Composable
fun CertificatesSection(state: PKIViewModel) {
    PKICard(
        title = "Certificates",
        icon = Icons.Default.Security
    ) {
        // Is already displayed in the general section.
        // Maybe combine General and Certificate section?
        val totalCerts = state.totalCertificates
        if (totalCerts > 0) {
            PKIItem("Total Certificates", totalCerts.toString())

            val personsWithCerts = state.knownPersons.filter { person ->
                val personId = person.userID?.toString() ?: ""
                val certificates = state.getCertificatesByPerson(personId)
                certificates.isNotEmpty()
            }

            PKIItem("Persons with Certificates", personsWithCerts.size.toString())
        }

        Spacer(modifier = Modifier.height(12.dp))

        val context = LocalContext.current
        OutlinedButton(
            onClick = {
                val intent = android.content.Intent(
                    context,
                    CertificatesActivity::class.java
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("View All Certificates")
        }
    }
}

@Composable
fun PKICard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun PKIItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ClickablePKIValue(
    label: String,
    displayValue: String,
    fullValue: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable {
                val clipData = AnnotatedString(fullValue)
                clipboardManager.setText(clipData)
                Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PKIScreenErrorPreview() {
    SharkNetMessengerAndroidTheme {
        PKIScreen()
    }
}
