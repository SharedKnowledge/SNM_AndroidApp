package net.sharksystem.sharknetmessengerandroid.ui.pki.certificates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.sharksystem.asap.ASAPEncounterConnectionType
import net.sharksystem.asap.pki.ASAPCertificate
import net.sharksystem.asap.pki.ASAPStorageAddress
import net.sharksystem.sharknetmessengerandroid.ui.theme.SharkNetMessengerAndroidTheme
import java.security.PublicKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CertificatesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SharkNetMessengerAndroidTheme {
                CertificatesScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(
    onBackPressed: () -> Unit = {}
) {
    var uiState by remember { mutableStateOf<CertificatesViewModel?>(null) }
    var selectedFilter by remember { mutableStateOf(CertificateFilter.ALL) }

    LaunchedEffect(Unit) {
        uiState = CertificatesViewModel()
    }

    // Whenever the filters are changed re-load the activity
    LaunchedEffect(selectedFilter) {
        uiState?.filterCertificates(selectedFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificates") },
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
                        uiState?.refresh()
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
                modifier = Modifier.padding(16.dp)
            ) {
                uiState?.let { state ->
                    if (state.isLoading) {
                        LoadingCertificatesSection()
                    } else if (state.hasError()) {
                        ErrorCertificatesSection(state.errorMessage)
                    } else {
                        CertificateStatsCard(state)

                        Spacer(modifier = Modifier.height(16.dp))

                        FilterChipsRow(
                            selectedFilter = selectedFilter,
                            onFilterChanged = { selectedFilter = it },
                            state = state
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.filteredCertificates.isEmpty()) {
                            NoCertificatesCard(selectedFilter)
                        } else {
                            CertificatesList(state.filteredCertificates)
                        }
                    }
                } ?: run {
                    LoadingCertificatesSection()
                }
            }
        }
    }
}

@Composable
fun CertificateStatsCard(state: CertificatesViewModel) {
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
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Certificate Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = state.totalCertificates.toString(),
                    label = "Total",
                    icon = Icons.Default.Key,
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = state.certificatesIssuedByMe.toString(),
                    label = "Issued by Me",
                    icon = Icons.Default.VerifiedUser,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    value = state.certificatesIssuedForMe.toString(),
                    label = "Issued for Me",
                    icon = Icons.Default.Verified,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedFilter: CertificateFilter,
    onFilterChanged: (CertificateFilter) -> Unit,
    state: CertificatesViewModel
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedFilter == CertificateFilter.ALL,
                onClick = { onFilterChanged(CertificateFilter.ALL) },
                label = { Text("All (${state.totalCertificates})") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == CertificateFilter.ISSUED_BY_ME,
                onClick = { onFilterChanged(CertificateFilter.ISSUED_BY_ME) },
                label = { Text("Issued by Me (${state.certificatesIssuedByMe})") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == CertificateFilter.ISSUED_FOR_ME,
                onClick = { onFilterChanged(CertificateFilter.ISSUED_FOR_ME) },
                label = { Text("Issued for Me (${state.certificatesIssuedForMe})") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun CertificatesList(certificates: List<CertificateItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(certificates) { certificate ->
            CertificateCard(certificate)
        }
    }
}

@Composable
fun CertificateCard(certificate: CertificateItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = certificate.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subjectId = certificate.subjectId
                    Text(
                        "Subject: ${if (subjectId.length > 25) subjectId.take(25) + "..." else subjectId}",

                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (certificate.isIssuedByMe)
                        Icons.Default.VerifiedUser
                    else
                        Icons.Default.Verified,
                    contentDescription = if (certificate.isIssuedByMe)
                        "Issued by me"
                    else
                        "Issued by others",
                    tint = if (certificate.isIssuedByMe)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Issued by:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = certificate.issuerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Valid since:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = certificate.validSince,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCertificatesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading certificates...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorCertificatesSection(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error Loading Certificates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NoCertificatesCard(filter: CertificateFilter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filter) {
                    CertificateFilter.ALL -> "No Certificates Found"
                    CertificateFilter.ISSUED_BY_ME -> "No Certificates Issued by You"
                    CertificateFilter.ISSUED_FOR_ME -> "No Certificates Issued for You"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (filter) {
                    CertificateFilter.ALL -> "Start by connecting with peers and exchanging certificates"
                    CertificateFilter.ISSUED_BY_ME -> "You haven't issued any certificates yet"
                    CertificateFilter.ISSUED_FOR_ME -> "No certificates have been issued for you by other peers"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CertificatesScreenPreview() {
    SharkNetMessengerAndroidTheme {
        CertificatesScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CertificatesScreenWithDataPreview() {
    SharkNetMessengerAndroidTheme {
        val mockCertificates = createMockCertificates()

        var selectedFilter by remember { mutableStateOf(CertificateFilter.ALL) }

        Column(
            modifier = Modifier.padding(16.dp)
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Certificate Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = "6",
                            label = "Total",
                            icon = Icons.Default.Key,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            value = "3",
                            label = "Issued by Me",
                            icon = Icons.Default.VerifiedUser,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        StatItem(
                            value = "2",
                            label = "Issued for Me",
                            icon = Icons.Default.Verified,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == CertificateFilter.ALL,
                        onClick = { selectedFilter = CertificateFilter.ALL },
                        label = { Text("All (6)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFilter == CertificateFilter.ISSUED_BY_ME,
                        onClick = { selectedFilter = CertificateFilter.ISSUED_BY_ME },
                        label = { Text("Issued by Me (3)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFilter == CertificateFilter.ISSUED_FOR_ME,
                        onClick = { selectedFilter = CertificateFilter.ISSUED_FOR_ME },
                        label = { Text("Issued for Me (2)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredCerts = when (selectedFilter) {
                CertificateFilter.ALL -> mockCertificates
                CertificateFilter.ISSUED_BY_ME -> mockCertificates.filter { it.isIssuedByMe }
                CertificateFilter.ISSUED_FOR_ME -> mockCertificates.filter { it.isIssuedForMe }
            }

            if (filteredCerts.isEmpty()) {
                NoCertificatesCard(selectedFilter)
            } else {
                CertificatesList(filteredCerts)
            }
        }
    }
}

private fun createMockCertificates(): List<CertificateItem> {
    val mockCerts = listOf(
        createMockCertificate(
            subjectName = "My Account",
            subjectId = "owner123456789",
            issuerName = "Alice",
            issuerId = "alice123456789",
            validSince = "15.06.2025 14:30",
            isIssuedByMe = false,
            isIssuedForMe = true
        ),
        createMockCertificate(
            subjectName = "My Account",
            subjectId = "owner123456789",
            issuerName = "Bob",
            issuerId = "bob987654321",
            validSince = "12.06.2025 09:15",
            isIssuedByMe = false,
            isIssuedForMe = true
        ),
        createMockCertificate(
            subjectName = "Alice",
            subjectId = "alice123456789",
            issuerName = "You",
            issuerId = "owner123456789",
            validSince = "20.06.2025 16:45",
            isIssuedByMe = true,
            isIssuedForMe = false
        ),
        createMockCertificate(
            subjectName = "Bob",
            subjectId = "bob987654321",
            issuerName = "You",
            issuerId = "owner123456789",
            validSince = "18.06.2025 11:20",
            isIssuedByMe = true,
            isIssuedForMe = false
        ),
        createMockCertificate(
            subjectName = "Charlie",
            subjectId = "charlie456789123",
            issuerName = "You",
            issuerId = "owner123456789",
            validSince = "16.06.2025 13:10",
            isIssuedByMe = true,
            isIssuedForMe = false
        ),
        createMockCertificate(
            subjectName = "Diana",
            subjectId = "diana789123456",
            issuerName = "Eve",
            issuerId = "eve321654987",
            validSince = "10.06.2025 08:30",
            isIssuedByMe = false,
            isIssuedForMe = false
        )
    )
    return mockCerts.sortedByDescending { it.validSince }
}

private fun createMockCertificate(
    subjectName: String,
    subjectId: String,
    issuerName: String,
    issuerId: String,
    validSince: String,
    isIssuedByMe: Boolean,
    isIssuedForMe: Boolean
): CertificateItem {
    val mockCert = object : ASAPCertificate {
        override fun getSubjectID(): CharSequence = subjectId
        override fun getIssuerID(): CharSequence = issuerId
        override fun getSubjectName(): CharSequence = subjectName
        override fun getIssuerName(): CharSequence = issuerName
        override fun getValidSince(): Calendar {
            val cal = Calendar.getInstance()
            cal.time = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(validSince)!!
            return cal
        }

        override fun getValidUntil(): Calendar {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, 1) // Valid for 1 year
            return cal
        }

        override fun getPublicKey(): PublicKey? = null
        override fun asBytes(): ByteArray = byteArrayOf()
        override fun verify(publicKeyIssuer: PublicKey?): Boolean = true
        override fun getASAPStorageAddress(): ASAPStorageAddress? = null
        override fun getConnectionTypeCredentialsReceived(): ASAPEncounterConnectionType? = null
        override fun isIdentical(asapCertificate: ASAPCertificate?): Boolean = false
    }

    return CertificateItem(
        certificate = mockCert,
        subjectName = subjectName,
        subjectId = subjectId,
        issuerName = issuerName,
        issuerId = issuerId,
        validSince = validSince,
        isIssuedByMe = isIssuedByMe,
        isIssuedForMe = isIssuedForMe
    )
}
