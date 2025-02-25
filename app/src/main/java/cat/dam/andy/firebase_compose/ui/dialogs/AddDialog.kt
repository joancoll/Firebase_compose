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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Afegeix contacte") },
            text = {
                Column {
                    val focusManager = LocalFocusManager.current
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = lastname,
                        onValueChange = { lastname = it },
                        label = { Text("Cognom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onConfirm(name, lastname)
                            onDismissRequest() // Tanca el diàleg
                        })
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onConfirm(name, lastname)
                    onDismissRequest() // Tanca el diàleg
                }) {
                    Text("Afegeix")
                }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Cancel·la")
                }
            }
        )
    }
}