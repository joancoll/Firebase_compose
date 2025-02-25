import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.*
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import cat.dam.andy.firebase_compose.R
import cat.dam.andy.firebase_compose.viewmodel.AuthViewModel

@Composable
fun AuthScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val authState by authViewModel.authState.collectAsState()

    // Observar el flux de missatges de Snackbar
    val snackbarMessage by authViewModel.snackbarMessage.collectAsState()

    // Mostrar Snackbar quan hi ha un missatge
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.clearSnackbarMessage()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Observar l'estat de l'autenticació
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                val user = (authState as AuthViewModel.AuthState.Success).user
                if (user.isEmailVerified) {
                    // L'usuari ha verificat el correu electrònic, permetre l'accés
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    // L'usuari no ha verificat el correu electrònic, mostrar un missatge
                    snackbarHostState.showSnackbar("Si us plau, verifica el teu correu electrònic abans d'entrar.")
                    authViewModel.signOut() // Tanca la sessió fins que es verifiqui
                }
            }
            is AuthViewModel.AuthState.Error -> {
                val errorMessage = (authState as AuthViewModel.AuthState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
                authViewModel.clearError()
            }
            is AuthViewModel.AuthState.Anonymous -> {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            else -> {
                // No fer res
            }
        }
    }

    fun startGoogleSignIn() {
        coroutineScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    // Autenticació amb Google Credentials per SDK >= 28
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val response = withContext(Dispatchers.IO) {
                        credentialManager.getCredential(context, request)
                    }
                    handleSignInResponse(
                        response,
                        navController,
                        authViewModel,
                        coroutineScope,
                        snackbarHostState
                    )
                } else { // Autenticació amb GoogleSignIn per SDK < 28
                    val googleSignInClient = GoogleSignIn.getClient(
                        context,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                    )
                    val signInIntent = googleSignInClient.signInIntent
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(signInIntent)
                        val account = task.getResult(ApiException::class.java)
                        firebaseAuthWithGoogle(
                            account.idToken ?: "",
                            authViewModel,
                            coroutineScope,
                            snackbarHostState
                        )
                    } catch (e: ApiException) {
                        snackbarHostState.showSnackbar("Error en l'autenticació: ${e.message}")
                        authViewModel.clearError()
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error en l'autenticació: ${e.message}")
                authViewModel.clearError()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { startGoogleSignIn() }) {
                Text("Iniciar sessió amb Google")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier.focusRequester(emailFocusRequester)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contrasenya") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        authViewModel.signInWithEmail(email, password)
                    }
                ),
                modifier = Modifier.focusRequester(passwordFocusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.signInWithEmail(email, password) }) {
                Text("Inicia sessió amb correu electrònic")
            }
            Button(onClick = { authViewModel.signUpWithEmail(email, password) }) {
                Text("Nou usuari")
            }
            Button(onClick = { authViewModel.signInAnonymously() }) {
                Text("Entra anònimament")
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun handleSignInResponse(
    response: GetCredentialResponse,
    navController: NavController,
    authViewModel: AuthViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val credential = response.credential
    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Log.e(
                "TAG",
                "loginSuccess idToken: ${googleIdTokenCredential.idToken}  ${googleIdTokenCredential.displayName}"
            )
            authViewModel.signInWithGoogle(googleIdTokenCredential.idToken)
        } catch (e: GoogleIdTokenParsingException) {
            e.printStackTrace()
        }
    } else {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Tipus de credencial no suportat.")
            authViewModel.clearError()
        }
    }
}

private fun firebaseAuthWithGoogle(
    idToken: String,
    authViewModel: AuthViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    authViewModel.signInWithGoogle(idToken)
}