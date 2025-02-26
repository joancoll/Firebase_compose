package cat.dam.andy.firebase_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.FragmentActivity
import cat.dam.andy.firebase_compose.data.FirestoreDataBaseHelper
import cat.dam.andy.firebase_compose.navigation.AppNavigation
import cat.dam.andy.firebase_compose.viewmodel.AuthViewModel
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel

class MainActivity : FragmentActivity() {
    // en la versió de Biometrica cal que sigui FragmentActivity i no ComponentActivity
    // Inicialitzem els ViewModels
    private val userListViewModel: UserListViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialitzem el UserListViewModel amb l'ajudant de la base de dades
        val databaseHelper = FirestoreDataBaseHelper(this, userListViewModel)
        userListViewModel.initialize(databaseHelper)

        // Configuració de la interfície d'usuari amb Jetpack Compose
        setContent {
            MaterialTheme {
                // Passem tots dos ViewModels a AppNavigation
                AppNavigation(authViewModel, userListViewModel)
            }
        }
    }
}