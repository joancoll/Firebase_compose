import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cat.dam.andy.firebase_compose.ui.dialogs.AddDialog
import cat.dam.andy.firebase_compose.ui.dialogs.DeleteDialog
import cat.dam.andy.firebase_compose.ui.dialogs.EditDialog
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cat.dam.andy.firebase_compose.ui.dialogs.DialogState
import cat.dam.andy.firebase_compose.ui.screens.DataView
import cat.dam.andy.firebase_compose.ui.screens.FilterField
import cat.dam.andy.firebase_compose.viewmodel.AuthViewModel

@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel
) {
    // Observa l'estat de l'autenticació
    val authState by authViewModel.authState.collectAsState()

    // Observa l'estat dels diàlegs
    val dialogState by userListViewModel.dialogState.collectAsState()

    var filter by remember { mutableStateOf("") }

    // Si l'usuari no està autenticat, redirigeix a la pantalla d'autenticació, excepte accés anònim
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Anonymous -> {
                // No fem res, l'usuari està en estat anònim
            }
            is AuthViewModel.AuthState.Idle, is AuthViewModel.AuthState.Error -> {
                if (navController.currentDestination?.route != "auth") {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }
            is AuthViewModel.AuthState.Success -> {
                if (navController.currentDestination?.route != "main") {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
            else -> {
                // No fem res per altres estats
            }
        }
    }


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
            when (authState) {
                is AuthViewModel.AuthState.Anonymous -> {
                    Button(
                        onClick = {
                            authViewModel.clearState()
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Torna a l'autenticació")
                    }
                }

                is AuthViewModel.AuthState.Success -> {
                    val user = (authState as AuthViewModel.AuthState.Success).user
                    Button(
                        onClick = {
                            authViewModel.signOut()
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Tanca sessió\n(${user.email ?: user.displayName})")
                    }
                }

                else -> {
                    // No es mostra cap botó per a altres estats
                }
            }

            // Filtre per la llista d'usuaris
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

            // Llista de dades
            DataView(
                userListViewModel = userListViewModel,
                onEditClick = { item -> userListViewModel.showEditDialog(item) },
                onDeleteClick = { item -> userListViewModel.showDeleteDialog(item) }
            )
        }

        // Botó per afegir nous elements
        FloatingActionButton(
            onClick = { userListViewModel.showAddDialog() },
            modifier = Modifier.align(Alignment.BottomEnd),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }

        // Diàlegs per afegir, editar i eliminar elements
        when (val currentState = dialogState) {
            is DialogState.Add -> {
                AddDialog(
                    onDismissRequest = { userListViewModel.dismissDialog() },
                    onConfirm = { name, lastname ->
                        userListViewModel.addContact(name, lastname) { success, message ->
                            if (success) {
                                userListViewModel.dismissDialog()
                            }
                        }
                    }
                )
            }

            is DialogState.Edit -> {
                EditDialog(
                    item = currentState.item,
                    onDismissRequest = { userListViewModel.dismissDialog() },
                    onConfirm = { item, name, lastname ->
                        userListViewModel.updateContact(item, name, lastname) { success, message ->
                            if (success) {
                                userListViewModel.dismissDialog()
                            }
                        }
                    }
                )
            }

            is DialogState.Delete -> {
                DeleteDialog(
                    item = currentState.item,
                    onDismissRequest = { userListViewModel.dismissDialog() },
                    onConfirm = { item ->
                        userListViewModel.deleteContact(item) { success, message ->
                            if (success) {
                                userListViewModel.dismissDialog()
                            }
                        }
                    }
                )
            }

            is DialogState.None -> {
                // No mostra cap diàleg
            }
        }
    }
}