package com.example.helppet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PerfilScreen(
                    onBackClick = { finish() },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onBackClick: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val userId = auth.currentUser?.uid

    var userName by remember { mutableStateOf("Carregando...") }
    val userEmail = auth.currentUser?.email ?: ""
    val creationTimestamp = auth.currentUser?.metadata?.creationTimestamp ?: 0L

    // Variáveis para guardar as preferências do alerta
    var savedAlertaEspecie by remember { mutableStateOf("Qualquer") }
    var savedAlertaRaio by remember { mutableStateOf("50 km") }

    val memberSince = remember(creationTimestamp) {
        if (creationTimestamp > 0) {
            SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR")).format(Date(creationTimestamp))
        } else {
            "Desconhecido"
        }
    }

    // Cores da Paleta HelpPet
    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textDark = Color(0xFF1C2723)
    val textHint = Color(0xFF9E9E9E)
    val colorAccentRed = Color(0xFFC53929)

    var showAlertaDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Vai buscar os dados do utilizador e os alertas guardados
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("nome") ?: "Utilizador"
                    // Tenta ler os alertas se já existirem, senão usa os valores por omissão
                    savedAlertaEspecie = doc.getString("alertaEspecie") ?: "Qualquer"
                    savedAlertaRaio = doc.getString("alertaRaio") ?: "50 km"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // --- CABEÇALHO ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_seta),
                contentDescription = "Voltar",
                tint = textDark,
                modifier = Modifier.size(24.dp).clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Perfil", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // --- INFORMAÇÕES DA CONTA ---
        Text(
            text = "INFORMAÇÕES DA CONTA",
            color = textHint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Surface(color = Color.White) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                InfoRow(label = "Nome", value = userName)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Email", value = userEmail)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Membro desde", value = memberSince)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ALERTAS ---
        Surface(color = Color.White, modifier = Modifier.clickable { showAlertaDialog = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = primaryGreen)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Alertas por região", color = textDark, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(">", color = textHint, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- LEGAL ---
        Text(
            text = "LEGAL",
            color = textHint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Surface(color = Color.White) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Política de Privacidade",
                    color = primaryGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Text(
                    text = "Termos de Uso",
                    color = primaryGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÕES DE SAÍDA ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            OutlinedButton(
                onClick = onLogout,
                border = BorderStroke(1.dp, primaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sair", color = primaryGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { },
                border = BorderStroke(1.dp, colorAccentRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Excluir minha conta", color = colorAccentRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // --- BOTTOM SHEET DO ALERTA ---
    if (showAlertaDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAlertaDialog = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight()
        ) {
            AlertaRegiaoSheet(
                initialEspecie = savedAlertaEspecie, // Passa a espécie guardada
                initialRaio = savedAlertaRaio,       // Passa o raio guardado
                modifier = Modifier.fillMaxHeight(),
                onDismiss = { showAlertaDialog = false },
                onCriarAlerta = { especie, raio ->
                    showAlertaDialog = false

                    // Lógica para salvar o alerta no Firebase!
                    if (userId != null) {
                        val dadosAlerta = mapOf(
                            "alertaEspecie" to especie,
                            "alertaRaio" to raio
                        )
                        // Usa o SetOptions.merge() para não apagar os outros dados (nome, telefone)
                        db.collection("usuarios").document(userId)
                            .set(dadosAlerta, SetOptions.merge())
                            .addOnSuccessListener {
                                savedAlertaEspecie = especie
                                savedAlertaRaio = raio
                                Toast.makeText(context, "Alerta configurado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Erro ao configurar alerta.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF1C2723), fontSize = 16.sp)
        Text(text = value, color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AlertaRegiaoSheet(
    initialEspecie: String,
    initialRaio: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onCriarAlerta: (String, String) -> Unit
) {
    // Inicia a aba com os valores que vieram da base de dados!
    var especieSelecionada by remember { mutableStateOf(initialEspecie) }
    var raioSelecionado by remember { mutableStateOf(initialRaio) }

    val primaryGreen = colorResource(id = R.color.primary_green)
    val textDark = Color(0xFF1C2723)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alerta de Pets Perdidos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textDark)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = textDark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Receba uma notificação quando um pet perdido for registrado na sua região.",
                color = primaryGreen,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Espécie", fontWeight = FontWeight.Bold, color = textDark, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val especies = listOf("Qualquer", "Cachorro", "Gato", "Pássaro")
            especies.forEach { esp ->
                FilterChip(
                    selected = especieSelecionada == esp,
                    onClick = { especieSelecionada = esp },
                    label = { Text(esp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Raio de busca", fontWeight = FontWeight.Bold, color = textDark, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val raios = listOf("10 km", "25 km", "50 km", "100 km")
            raios.forEach { raio ->
                FilterChip(
                    selected = raioSelecionado == raio,
                    onClick = { raioSelecionado = raio },
                    label = { Text(raio) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCriarAlerta(especieSelecionada, raioSelecionado) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Alerta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}