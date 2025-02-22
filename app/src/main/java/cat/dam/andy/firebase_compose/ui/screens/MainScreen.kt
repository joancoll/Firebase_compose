package cat.dam.andy.firebase_compose.ui.screens

import cat.dam.andy.firebase_compose.ui.dialogs.DialogState

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.dam.andy.firebase_compose.model.Item
import cat.dam.andy.firebase_compose.ui.dialogs.AddDialog
import cat.dam.andy.firebase_compose.ui.dialogs.DeleteDialog
import cat.dam.andy.firebase_compose.ui.dialogs.EditDialog
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel

@Composable
fun MainScreen(
    userListViewModel: UserListViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Item) -> Unit,
    onDeleteClick: (Item) -> Unit,
    dialogState: DialogState,
    onDismissDialog: () -> Unit,
    onConfirmAdd: (String, String) -> Unit,
    onConfirmEdit: (Item, String, String) -> Unit,
    onConfirmDelete: (Item) -> Unit
) {
    var filter by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FilterField(
                filter = filter,
                onFilterChange = { newFilter ->
                    filter = newFilter
                    userListViewModel.updateFilter(newFilter)
                },
                onClearFilter = {
                    filter = ""
                    userListViewModel.updateFilter("")
                }
            )
            // Fem servir DataView passant el ViewModel i les funcions de callback
            DataView(
                userListViewModel = userListViewModel,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }

        when (val currentState = dialogState) {
            is DialogState.Add -> {
                AddDialog(onDismissRequest = onDismissDialog, onConfirm = onConfirmAdd)
            }
            is DialogState.Edit -> {
                EditDialog(item = currentState.item, onDismissRequest = onDismissDialog, onConfirm = onConfirmEdit)
            }
            is DialogState.Delete -> {
                DeleteDialog(item = currentState.item, onDismissRequest = onDismissDialog, onConfirm = onConfirmDelete)
            }
            is DialogState.None -> {
                // No mostra cap diàleg
            }
        }
    }
}