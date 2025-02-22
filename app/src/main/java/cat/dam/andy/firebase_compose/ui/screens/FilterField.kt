package cat.dam.andy.firebase_compose.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FilterField(
    filter: String,
    onFilterChange: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    val localKeyboardController = LocalSoftwareKeyboardController.current
    val keyboardController = localKeyboardController ?: return

    OutlinedTextField(
        value = filter,
        onValueChange = {
            onFilterChange(it)
        },
        label = { Text("Filtrar") },
        trailingIcon = {
            IconButton(onClick = {
                keyboardController.hide()
                onClearFilter()
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Netejar cerca"
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController.hide()
        }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}