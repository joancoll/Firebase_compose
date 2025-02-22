package cat.dam.andy.firebase_compose.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.dam.andy.firebase_compose.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(
    item: Item,
    onDismissRequest: () -> Unit,
    onConfirm: (Item, String, String) -> Unit
) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedLastname by remember { mutableStateOf(item.lastname) }

    Dialog(onDismissRequest = onDismissRequest) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Editar contacte") },
            text = {
                Column {
                    val focusManager = LocalFocusManager.current
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editedLastname,
                        onValueChange = { editedLastname = it },
                        label = { Text("Cognoms") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onConfirm(item, editedName, editedLastname)
                            onDismissRequest() // Tanca el diàleg
                        })
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onConfirm(item, editedName, editedLastname)
                    onDismissRequest() // Tanca el diàleg
                }) {
                    Text("Guardar canvis")
                }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}