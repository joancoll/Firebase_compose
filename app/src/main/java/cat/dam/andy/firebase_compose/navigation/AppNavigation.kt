package cat.dam.andy.firebase_compose.navigation

import AuthScreen
import MainScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel
import cat.dam.andy.firebase_compose.viewmodel.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel, userListViewModel: UserListViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(navController, authViewModel)
        }
        composable("main") {
            MainScreen(navController, authViewModel, userListViewModel)
        }
    }
}