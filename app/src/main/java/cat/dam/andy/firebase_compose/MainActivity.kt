package cat.dam.andy.firebase_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import cat.dam.andy.firebase_compose.data.FirestoreDataBaseHelper
import cat.dam.andy.firebase_compose.ui.screens.MainScreen
import cat.dam.andy.firebase_compose.ui.theme.Firebase_composeTheme
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel

class MainActivity : ComponentActivity() {
    private val userListViewModel: UserListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialitzem el ViewModel amb l'ajudant de la base de dades
        val databaseHelper = FirestoreDataBaseHelper(this, userListViewModel)
        userListViewModel.initialize(databaseHelper)

        setContent {
            Firebase_composeTheme {
                MainScreen(
                    userListViewModel = userListViewModel,
                    onAddClick = { userListViewModel.showAddDialog() },
                    onEditClick = { item -> userListViewModel.showEditDialog(item) },
                    onDeleteClick = { item -> userListViewModel.showDeleteDialog(item) },
                    dialogState = userListViewModel.dialogState,
                    onDismissDialog = { userListViewModel.dismissDialog() },
                    onConfirmAdd = { name, lastname ->
                        userListViewModel.addContact(name, lastname) { success, message ->
                            if (success) {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                userListViewModel.dismissDialog()
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onConfirmEdit = { item, name, lastname ->
                        userListViewModel.updateContact(item, name, lastname) { success, message ->
                            if (success) {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                userListViewModel.dismissDialog()
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onConfirmDelete = { item ->
                        userListViewModel.deleteContact(item) { success, message ->
                            if (success) {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                userListViewModel.dismissDialog()
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}