package cat.dam.andy.firebase_compose.ui.dialogs

import cat.dam.andy.firebase_compose.model.Item

sealed class DialogState {
    // Controla en quin estat es troba el diàleg (afegir, editar, eliminar o cap)
    object None : DialogState()
    object Add : DialogState()
    data class Edit(val item: Item) : DialogState()
    data class Delete(val item: Item) : DialogState()
}