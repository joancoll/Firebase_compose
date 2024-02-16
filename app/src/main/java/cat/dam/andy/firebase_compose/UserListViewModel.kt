package cat.dam.andy.firebase_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {
    private val _userListState = MutableStateFlow<List<Item>>(emptyList())
    val userListState: StateFlow<List<Item>> get() = _userListState

    fun updateUserList(newUserList: List<Item>) {
        viewModelScope.launch {
            _userListState.value = newUserList
        }
    }
}
