package cat.dam.andy.firebase_compose.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.dam.andy.firebase_compose.data.FirestoreDataBaseHelper
import cat.dam.andy.firebase_compose.model.Item
import cat.dam.andy.firebase_compose.model.ItemComparator
import cat.dam.andy.firebase_compose.ui.dialogs.DialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {
    // Estat del diàleg
    var dialogState by mutableStateOf<DialogState>(DialogState.None)
        private set

    // Estat de la llista d'usuaris
    private val _userListState = MutableStateFlow<List<Item>>(emptyList())
    val userListState: StateFlow<List<Item>> = _userListState.asStateFlow()

    // Filtre actual
    var filter by mutableStateOf("")
        private set

    // Base de dades
    private lateinit var databaseHelper: FirestoreDataBaseHelper

    // Inicialitzem el ViewModel
    fun initialize(databaseHelper: FirestoreDataBaseHelper) {
        this.databaseHelper = databaseHelper
        loadItems()
    }

    // Actualitza la llista d'usuaris
    fun updateUserList(items: List<Item>) {
        _userListState.value = items
    }

    // Actualitza el filtre i recarrega la llista d'usuaris
    fun updateFilter(newFilter: String) {
        filter = newFilter
        loadItems()
    }

    // Carrega els ítems de la base de dades segons el filtre actual
    private fun loadItems() {
        viewModelScope.launch {
            databaseHelper.getContacts(filter) { success, items ->
                if (success) {
                    _userListState.value = items.sortedWith(ItemComparator())
                } else {
                    // Manejar l'error
                }
            }
        }
    }

    fun addContact(name: String, lastname: String, onResult: (Boolean, String) -> Unit) {
        if (name.isNotBlank() && name.length >= 2) {
            val newItem = Item(name = name, lastname = lastname)
            databaseHelper.addContact(newItem) { success, message ->
                if (success) {
                    loadItems() // Recarrega la llista després d'afegir
                }
                onResult(success, message)
            }
        } else {
            onResult(false, "El nom ha de tenir com a mínim 2 caràcters")
        }
    }

    fun updateContact(item: Item, name: String, lastname: String, onResult: (Boolean, String) -> Unit) {
        if (name.isNotBlank()  && name.length >= 2 ) {
            val editedItem = Item(name = name, lastname = lastname)
            databaseHelper.updateContact(item, editedItem) { success, message ->
                if (success) {
                    loadItems() // Recarrega la llista després d'editar
                }
                onResult(success, message)
            }
        } else {
            onResult(false, "El nom ha de tenir com a mínim 2 caràcters")
        }
    }

    // Eliminar un contacte
    fun deleteContact(item: Item, onResult: (Boolean, String) -> Unit) {
        databaseHelper.removeContact(item) { success, message ->
            if (success) {
                loadItems() // Recarrega la llista després d'eliminar
            }
            onResult(success, message)
        }
    }

    // Mostrar el diàleg per afegir
    fun showAddDialog() {
        dialogState = DialogState.Add
    }

    // Mostrar el diàleg per editar
    fun showEditDialog(item: Item) {
        dialogState = DialogState.Edit(item)
    }

    // Mostrar el diàleg per eliminar
    fun showDeleteDialog(item: Item) {
        dialogState = DialogState.Delete(item)
    }

    // Tancar el diàleg
    fun dismissDialog() {
        dialogState = DialogState.None
    }
}