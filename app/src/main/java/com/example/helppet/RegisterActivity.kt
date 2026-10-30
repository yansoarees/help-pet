package com.example.helppet

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                RegisterScreen(
                    onBackToLogin = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBackToLogin: () -> Unit) {
    val context = LocalContext.current

    // INSTÂNCIAS DO FIREBASE
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // ESTADO DE CARREGAMENTO
    var isLoading by remember { mutableStateOf(false) }

    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)
    val primaryGreen = colorResource(id = R.color.primary_green)
    val lightGrayBorder = Color(0xFFE0E0E0)
    val darkGreenText = Color(0xFF0D4722)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundOffWhite)
    ) {
        Image(
            painter = painterResource(id = R.drawable.patalogo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-40).dp, y = 60.dp)
                .alpha(0.15f)
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo HelpPet",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                placeholder = { Text("Nome completo", color = Color.Gray) },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_email), contentDescription = null, tint = Color.DarkGray)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = nameError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = lightGrayBorder,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            if (nameError) Text("Insira o seu nome completo", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = false },
                placeholder = { Text("E-mail", color = Color.Gray) },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_email), contentDescription = null, tint = Color.DarkGray)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = lightGrayBorder,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            if (emailError) Text("Insira um e-mail válido", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = false },
                placeholder = { Text("Telefone / WhatsApp", color = Color.Gray) },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_phone), contentDescription = null, tint = Color.DarkGray)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = lightGrayBorder,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            if (phoneError) Text("Insira um número válido (mín 8 dígitos)", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = false },
                placeholder = { Text("Senha", color = Color.Gray) },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.ic_senha), contentDescription = null, tint = Color.DarkGray)
                },
                trailingIcon = {
                    val icon = if (isPasswordVisible) R.drawable.ic_email else R.drawable.ic_senha
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(painterResource(id = icon), contentDescription = "Mostrar Senha", tint = Color.Gray)
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = lightGrayBorder,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            if (passwordError) Text("A senha deve ter pelo menos 6 caracteres", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isEmpty()) { nameError = true; return@Button }
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailError = true; return@Button }
                    if (phone.isEmpty() || phone.length < 8) { phoneError = true; return@Button }
                    if (password.isEmpty() || password.length < 6) { passwordError = true; return@Button }

                    isLoading = true

                    // LÓGICA DO FIREBASE AQUI
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    val userMap = hashMapOf(
                                        "nome" to name,
                                        "email" to email,
                                        "telefone" to phone
                                    )
                                    db.collection("usuarios").document(userId).set(userMap)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                            onBackToLogin()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Conta criada, mas erro ao salvar dados.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = darkGreenText),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Cadastrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}