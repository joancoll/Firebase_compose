package cat.dam.andy.firebase_compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Estat de l'autenticació
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState() // Inicial o inactiu
        object Loading : AuthState() // S'està carregant
        data class Success(val user: FirebaseUser) : AuthState() // Autenticació exitosa
        data class Error(val message: String) : AuthState() // Error en l'autenticació
        data class Anonymous(val user: FirebaseUser) : AuthState() // Autenticació anònima
    }

    // Flux per als missatges de Snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    // Funció per netejar el missatge de Snackbar
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    // Funció per mostrar un missatge de Snackbar
    private fun showSnackbarMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearState() {
        _authState.value = AuthState.Idle
        signOut()
    }

    // Funció per netejar l'estat d'error
    fun clearError() {
        _authState.value = AuthState.Idle
    }

    // Autenticació amb correu electrònic
    fun signInWithEmail(email: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Format d'email incorrecte.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contrasenya ha de tenir com a mínim 6 caràcters.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                _authState.value = AuthState.Success(user!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en iniciar sessió: ${e.message}")
            }
        }
    }

    // Autenticació amb Google
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                val user = auth.currentUser
                _authState.value = AuthState.Success(user!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en l'autenticació amb Google: ${e.message}")
            }
        }
    }

    // Registrar-se amb correu electrònic
    fun signUpWithEmail(email: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Format d'email incorrecte.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contrasenya ha de tenir com a mínim 6 caràcters.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Registrar l'usuari
                auth.createUserWithEmailAndPassword(email, password).await()
                val user = auth.currentUser

                // Enviar correu de verificació
                user?.sendEmailVerification()?.await()

                // Mostrar missatge a l'usuari
                _authState.value = AuthState.Error("S'ha enviat un correu de verificació. Si us plau, verifica el teu correu electrònic.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en registrar l'usuari: ${e.message}")
            }
        }
    }

    // Tancar sessió
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // Accés anònim
    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInAnonymously().await()
                _authState.value = AuthState.Anonymous(auth.currentUser!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en l'accés anònim: ${e.message}")
            }
        }
    }
}