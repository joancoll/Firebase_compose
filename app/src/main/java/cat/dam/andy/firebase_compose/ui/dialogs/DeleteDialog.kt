package cat.dam.andy.firebase_compose.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import cat.dam.andy.firebase_compose.model.Item

@Composable
fun DeleteDialog(
    item: Item,
    onDismissRequest: () -> Unit,
    onConfirm: (Item) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Eliminar contacte") },
        text = { Text("Segur que vols eliminar el contacte ${item.name} ${item.lastname}?") },
        confirmButton = {
            Button(onClick = {
                onConfirm(item)
                onDismissRequest() // Tanca el diàleg
            }) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel·lar")
            }
        }
    )
}