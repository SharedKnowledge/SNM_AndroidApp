import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.sharksystem.SharkPeer
import net.sharksystem.asap.persons.PersonValues
import net.sharksystem.asap.persons.PersonValuesImpl
import net.sharksystem.pki.AndroidSharkPKIComponentImpl
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp

@Composable
fun RecipientSelectionScreen(
    knownPeers: Set<PersonValues> =
        AndroidSharkPKIComponentImpl(SharkNetApp.singleton!!.getPeer()).getPersons(),
    onSelectionConfirmed: (MutableSet<CharSequence>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPeers = remember { mutableStateOf(mutableSetOf<CharSequence>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipients for Encryption") },
        text = {
            LazyColumn {
                items(knownPeers.toList()) { peer ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedPeers.value.contains(peer.userID),
                            onCheckedChange = { checked ->
                                if (checked)
                                    selectedPeers.value + peer
                                else
                                    selectedPeers.value - peer
                            }
                        )
                        Text(peer.userID.toString())
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSelectionConfirmed(selectedPeers.value)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}