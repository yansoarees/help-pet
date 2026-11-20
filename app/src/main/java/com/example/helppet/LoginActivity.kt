package com.example.helppet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

private val VerdeBotao = Color(0xFF0D4722)
private val VermelhoAviso = Color(0xFFC53929)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var carregandoEmail by remember { mutableStateOf(false) }
    var carregandoGoogle by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var mostrarRecuperarSenha by remember { mutableStateOf(false) }
    val ocupado = carregandoEmail || carregandoGoogle

    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textSecondary = colorResource(id = R.color.text_secondary)

    val googleSignInClient = remember {
        val opcoes = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("633426246437-nemiqs5ob2gn4n7402q7c6oe1mtr5ems.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, opcoes)
    }

    val launcherGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val conta = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val token = conta?.idToken
            if (token == null) {
                carregandoGoogle = false
                erro = "Não foi possível entrar com o Google. Tente novamente."
            } else {
                auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                    .addOnSuccessListener {
                        criarPerfilSeFaltar()
                        carregandoGoogle = false
                        val primeiroNome = conta.displayName?.trim()?.split(" ")?.firstOrNull()
                        Toast.makeText(
                            context,
                            if (primeiroNome.isNullOrBlank()) "Bem-vindo(a)!" else "Bem-vindo(a), $primeiroNome!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onLoginSuccess()
                    }
                    .addOnFailureListener { e ->
                        carregandoGoogle = false
                        erro = mensagemErroAuth(e)
                    }
            }
        } catch (e: ApiException) {
            carregandoGoogle = false
            erro = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                CommonStatusCodes.NETWORK_ERROR -> "Sem conexão com a internet. Verifique sua rede e tente de novo."
                CommonStatusCodes.DEVELOPER_ERROR -> "Login com Google não está liberado neste aparelho. Use e-mail e senha."
                else -> "Não foi possível entrar com o Google. Tente novamente."
            }
        }
    }

    fun entrarComEmail() {
        focusManager.clearFocus()
        erro = when {
            email.isBlank() || password.isEmpty() -> "Preencha o e-mail e a senha."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "E-mail inválido. Exemplo: nome@email.com"
            else -> null
        }
        if (erro != null) return

        carregandoEmail = true
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                carregandoEmail = false
                onLoginSuccess()
            }
            .addOnFailureListener { e ->
                carregandoEmail = false
                erro = mensagemErroAuth(e)
            }
    }

    fun entrarComGoogle() {
        focusManager.clearFocus()
        erro = null
        carregandoGoogle = true
        googleSignInClient.signOut().addOnCompleteListener {
            launcherGoogle.launch(googleSignInClient.signInIntent)
        }
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryGreen,
        unfocusedBorderColor = Color(0xFFE3E6E5),
        focusedLabelColor = primaryGreen,
        focusedLeadingIconColor = primaryGreen,
        cursorColor = primaryGreen,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

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
                .alpha(0.12f)
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo HelpPet",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Bem-vindo de volta!", color = VerdeBotao, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Entre para ajudar mais pets a voltarem para casa.",
                color = textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; erro = null },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                enabled = !ocupado,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = coresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; erro = null },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                enabled = !ocupado,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { entrarComEmail() }),
                shape = RoundedCornerShape(14.dp),
                colors = coresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { mostrarRecuperarSenha = true }) {
                    Text("Esqueci minha senha", color = primaryGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            erro?.let { mensagem ->
                Surface(
                    color = Color(0xFFFCE8E6),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = VermelhoAviso, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(mensagem, color = VermelhoAviso, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { entrarComEmail() },
                enabled = !ocupado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeBotao,
                    disabledContainerColor = VerdeBotao.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (carregandoEmail) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Entrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                Text("ou", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { entrarComGoogle() },
                enabled = !ocupado,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (carregandoGoogle) {
                    CircularProgressIndicator(color = primaryGreen, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continuar com Google", color = Color(0xFF1F1F1F), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Não tem uma conta?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Cadastre-se", color = VerdeBotao, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (mostrarRecuperarSenha) {
        RecuperarSenhaDialog(emailInicial = email.trim(), onFechar = { mostrarRecuperarSenha = false })
    }
}

@Composable
private fun RecuperarSenhaDialog(emailInicial: String, onFechar: () -> Unit) {
    val context = LocalContext.current
    val primaryGreen = colorResource(id = R.color.primary_green)
    var emailRecuperar by remember { mutableStateOf(emailInicial) }
    var erro by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }

    fun enviarLink() {
        val destino = emailRecuperar.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(destino).matches()) {
            erro = "Digite um e-mail válido"
            return
        }
        enviando = true
        FirebaseAuth.getInstance().sendPasswordResetEmail(destino)
            .addOnSuccessListener {
                enviando = false
                Toast.makeText(context, "Enviamos um link para $destino. Confira também o spam.", Toast.LENGTH_LONG).show()
                onFechar()
            }
            .addOnFailureListener { e ->
                enviando = false
                erro = mensagemErroAuth(e)
            }
    }

    AlertDialog(
        onDismissRequest = { if (!enviando) onFechar() },
        containerColor = Color.White,
        title = { Text("Recuperar senha", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Digite o e-mail da sua conta. Vamos enviar um link para você criar uma senha nova.",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailRecuperar,
                    onValueChange = { emailRecuperar = it; erro = null },
                    label = { Text("E-mail") },
                    singleLine = true,
                    isError = erro != null,
                    supportingText = if (erro != null) {
                        { Text(erro ?: "") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { enviarLink() }),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { enviarLink() }, enabled = !enviando) {
                Text(if (enviando) "Enviando..." else "Enviar link", color = primaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar, enabled = !enviando) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
