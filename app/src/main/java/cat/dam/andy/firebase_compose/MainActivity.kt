package cat.dam.andy.firebase_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import cat.dam.andy.firebase_compose.ui.theme.Firebase_composeTheme

sealed class DialogState {
    // Controla en quin estat es troba el diàleg (afegir, editar, eliminar o cap)
    object None : DialogState()
    object Add : DialogState()
    data class Edit(val item: Item) : DialogState()
    data class Delete(val item: Item) : DialogState()
}

class MainActivity : ComponentActivity() {
    private lateinit var userListViewModel: UserListViewModel
    private lateinit var databaseHelper: FirestoreDataBaseHelper
    private var filter by mutableStateOf("")
    private var dialogState by mutableStateOf<DialogState>(DialogState.None)
    private var itemToDelete by mutableStateOf<Item?>(null)
    private var itemToEdit by mutableStateOf<Item?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userListViewModel = ViewModelProvider(this).get(UserListViewModel::class.java)
        databaseHelper = FirestoreDataBaseHelper(this, userListViewModel)

        setContent {
            Firebase_composeTheme {
                MyApp()
            }
        }
    }

    @Composable
    fun MyApp() {
        val focusManager = LocalFocusManager.current
        // Utilitza viewModel per obtenir el UserListViewModel
        val userListViewModel: UserListViewModel = viewModel()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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
                    FilterField()
                    DataView(onEditClick = { item ->
                        itemToEdit = item
                        dialogState = DialogState.Edit(item)
                    }, onDeleteClick = { item ->
                        itemToDelete = item
                        dialogState = DialogState.Delete(item)
                    })
                }

                FloatingActionButton(
                    onClick = {
                        dialogState = DialogState.Add
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
                when (val currentState = dialogState) {
                    is DialogState.Add -> {
                        ShowAddDialog()
                    }

                    is DialogState.Edit -> {
                        ShowEditDialog(item = currentState.item)
                    }

                    is DialogState.Delete -> {
                        ShowDeleteConfirmationDialog(item = currentState.item)
                    }

                    is DialogState.None -> {
                        // No mostra cap diàleg
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun FilterField() {
        val localKeyboardController = LocalSoftwareKeyboardController.current
        val keyboardController = localKeyboardController ?: return

        OutlinedTextField(
            value = filter,
            onValueChange = {
                filter = it
                updateResults() // Actualitza resultats quan el filtre canvia
            },
            label = { Text("Filtrar") },
            trailingIcon = {
                IconButton(onClick = {
                    keyboardController.hide()
                    filter = ""
                    updateResults() // Actualitza resultats quan el filtre es buida
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
                updateResults() // Actualitza resultats quan es prem "Done"
                keyboardController.hide()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        LaunchedEffect(filter) {
            // Actualitza automàticament quan el filtre canvia
            updateResults()
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowAddDialog() {
        var name by remember { mutableStateOf("") }
        var lastname by remember { mutableStateOf("") }

        Dialog(onDismissRequest = {
            // Tanca el Dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text("Afegir contacte")
                },
                text = {
                    Column {
                        // definim un focusManager dins de l'AlertDialog
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = { Text("Nom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Mou el focus al següent camp quan es prem "Next" al teclat
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = lastname,
                            onValueChange = {
                                lastname = it
                            },
                            label = { Text("Cognom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    createNewItem(name, lastname)
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            createNewItem(name, lastname)
                        }
                    ) {
                        Text("Afegir")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Tanca el Dialog
                            dialogState = DialogState.None
                        }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }

    @Composable
    private fun ShowDeleteConfirmationDialog(item: Item) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Eliminar contacte") },
            text = { Text("Segur que vols eliminar el contacte ${item.name} ${item.lastname}?") },
            confirmButton = {
                Button(
                    onClick = {
                        dialogState = DialogState.None
                        // Implementa la lògica d'eliminació a la base de dades
                        databaseHelper.removeContact(item) { success, itemResult ->
                            if (success) {
                                // No cal cridar updateResults() aquí ja que ja s'ha actualitzat el ViewModel
                            } else {
                                Toast.makeText(this, "Error: $itemResult", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { dialogState = DialogState.None }) {
                    Text("Cancel·lat!")
                }
            })
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowEditDialog(item: Item) {
        var editedName by remember { mutableStateOf(item.name) }
        var editedLastname by remember { mutableStateOf(item.lastname) }

        Dialog(onDismissRequest = {
            // Tanca el Dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text("Editar contacte")
                },
                text = {
                    Column {
                        // definim un focusManager dins de l'AlertDialog
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = editedName,
                            onValueChange = {
                                editedName = it
                            },
                            label = { Text("Nom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Mou el focus al següent camp quan es prem "Next" al teclat
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = editedLastname,
                            onValueChange = {
                                editedLastname = it
                            },
                            label = { Text("Cognoms") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    updateItem(item, editedName, editedLastname)
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateItem(item, editedName, editedLastname)
                        }
                    ) {
                        Text("Guardar canvis")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Tanca el Dialog sense desar els canvis
                            dialogState = DialogState.None
                        }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }

    private fun createNewItem(name: String, lastname: String) {
        if (isValidEntry(name, lastname)) {
            // Tanca el Dialog
            dialogState = DialogState.None
            val newItem = Item(name = name, lastname = lastname)
            databaseHelper.addContact(newItem) { success, newItemResult ->
                if (success) {
                    // No cal cridar updateResults() aquí ja que ja s'ha actualitzat el ViewModel
                } else {
                    Toast.makeText(this, "Error: $newItemResult", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.novalid_entry), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateItem(item: Item, name: String, lastname: String) {
        if (isValidEntry(name, lastname)) {
            // Tanca el Dialog
            dialogState = DialogState.None
            val editedItem = Item(name=name, lastname=lastname)
            databaseHelper.updateContact(item, editedItem) { success, editedItemResult ->
                if (success) {
                    // No cal cridar updateResults() aquí ja que ja s'ha actualitzat el ViewModel
                } else {
                    Toast.makeText(this, "Error: $editedItemResult", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.error_user), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateResults() {
        // Obté els resultats de la base de dades en funció del filtre actual
        databaseHelper.getContacts(filter) { success, items ->
            if (success) {
                // Ordena alfabèticament la llista d'usuaris (cal crear un objecte per actualitzar)
                val sortedItems = items.sortedWith(CustomComparator())
                // Utilitza el ViewModel per actualitzar les dades inicials amb una nova llista ordenada
                userListViewModel.updateUserList(sortedItems.toList())
            } else {
                Toast.makeText(this, "Error: $items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEntry(name: String, lastname: String): Boolean {
        return name.isNotBlank()
    }
}