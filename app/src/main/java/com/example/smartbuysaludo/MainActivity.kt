package com.example.smartbuysaludo

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartbuysaludo.ui.theme.SmartBuySaludoTheme
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartBuySaludoTheme {
                SmartBuyApp()
            }
        }
    }
}

@Composable
fun SmartBuyApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var usuario by remember { mutableStateOf(auth.currentUser) }
    var registro by remember { mutableStateOf(false) }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            usuario = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val sesion = usuario
    if (sesion != null) {
        key(sesion.uid) {
            TiendaSmartBuy(
                onCerrarSesion = {
                    auth.signOut()
                    correo = ""
                    clave = ""
                    confirmacion = ""
                    mensaje = ""
                    registro = false
                }
            )
        }
        return
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { espacio ->
        Column(
            modifier = Modifier
                .padding(espacio)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Smart Buy",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF008577)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (registro) "Crear cuenta" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                enabled = !cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !cargando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (registro) {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmacion,
                    onValueChange = { confirmacion = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    enabled = !cargando,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Text("Usa una contraseña de al menos 6 caracteres.")
            }

            if (mensaje.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val email = correo.trim()
                    mensaje = ""

                    when {
                        email.isBlank() || clave.isBlank() -> {
                            mensaje = "Completa el correo y la contraseña."
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            mensaje = "Escribe un correo electrónico válido."
                        }

                        registro && clave.length < 6 -> {
                            mensaje = "La contraseña debe tener al menos 6 caracteres."
                        }

                        registro && clave != confirmacion -> {
                            mensaje = "Las contraseñas no coinciden."
                        }

                        else -> {
                            cargando = true

                            val tarea = if (registro) {
                                auth.createUserWithEmailAndPassword(email, clave)
                            } else {
                                auth.signInWithEmailAndPassword(email, clave)
                            }

                            tarea.addOnCompleteListener { resultado ->
                                cargando = false

                                if (resultado.isSuccessful) {
                                    usuario = auth.currentUser
                                    clave = ""
                                    confirmacion = ""
                                    mensaje = ""
                                } else {
                                    mensaje = mensajeError(resultado.exception)
                                }
                            }
                        }
                    }
                }
            ) {
                Text(
                    if (cargando) "Procesando..."
                    else if (registro) "Registrarme"
                    else "Ingresar"
                )
            }

            TextButton(
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    registro = !registro
                    mensaje = ""
                    clave = ""
                    confirmacion = ""
                }
            ) {
                Text(
                    if (registro) "Ya tengo cuenta. Iniciar sesión"
                    else "No tengo cuenta. Registrarme"
                )
            }
        }
    }
}

fun mensajeError(error: Exception?): String {
    if (error is FirebaseNetworkException) {
        return "No se pudo conectar. Revisa tu conexión a internet."
    }

    return when ((error as? FirebaseAuthException)?.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" ->
            "Ese correo ya está registrado. Intenta iniciar sesión."

        "ERROR_WEAK_PASSWORD" ->
            "La contraseña no cumple los requisitos de seguridad."

        "ERROR_INVALID_EMAIL" ->
            "El correo electrónico no es válido."

        "ERROR_USER_DISABLED" ->
            "Esta cuenta está deshabilitada."

        "ERROR_TOO_MANY_REQUESTS" ->
            "Demasiados intentos. Espera unos minutos."

        "ERROR_OPERATION_NOT_ALLOWED" ->
            "Debes habilitar correo y contraseña en Firebase."

        "ERROR_INVALID_CREDENTIAL",
        "ERROR_WRONG_PASSWORD",
        "ERROR_USER_NOT_FOUND",
        "ERROR_INVALID_LOGIN_CREDENTIALS" ->
            "No se pudo ingresar. Revisa el correo y la contraseña."

        else ->
            "No se pudo completar la solicitud. Inténtalo nuevamente."
    }
}