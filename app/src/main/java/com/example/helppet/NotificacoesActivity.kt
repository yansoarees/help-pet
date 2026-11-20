package com.example.helppet

import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.foundation.BorderStroke
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notificacao(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val destinatarioId: String = "TODOS",
    val telefoneContato: String = "",
    val data: Long = System.currentTimeMillis()
)

object NotificacaoHelper {

    fun enviarAlerta(titulo: String, mensagem: String, destinatarioId: String = "TODOS", telefoneContato: String = "") {
        val db = FirebaseFirestore.getInstance()
        val novaNotificacao = hashMapOf(
            "titulo" to titulo,
            "mensagem" to mensagem,
            "destinatarioId" to destinatarioId,
            "telefoneContato" to telefoneContato,
            "data" to System.currentTimeMillis()
        )
        db.collection("notificacoes").add(novaNotificacao)
    }
}

object RegistroNotificacoes {
    private fun chave() = "ultima_visita_notificacoes_" + (FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo")

    fun ultimaVisita(context: Context): Long =
        context.getSharedPreferences("helppet_prefs", Context.MODE_PRIVATE).getLong(chave(), 0L)

    fun marcarComoVistas(context: Context) {
        context.getSharedPreferences("helppet_prefs", Context.MODE_PRIVATE)
            .edit().putLong(chave(), System.currentTimeMillis()).apply()
    }
}

fun telefoneDaNotificacao(notificacao: Notificacao): String {
    if (notificacao.telefoneContato.isNotBlank()) return notificacao.telefoneContato.filter { it.isDigit() }
    val achado = Regex("Conta(c)?to:\\s*([0-9 ()-]+)").find(notificacao.mensagem)
    return achado?.groupValues?.get(2)?.filter { it.isDigit() }.orEmpty()
}

class NotificacoesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val visitaAnterior = RegistroNotificacoes.ultimaVisita(this)
        RegistroNotificacoes.marcarComoVistas(this)

        setContent {
            MaterialTheme {
                NotificacoesScreen(visitaAnterior = visitaAnterior, onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(visitaAnterior: Long = 0L, onBackClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)

    var listaNotificacoes by remember { mutableStateOf<List<Notificacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val db = remember { FirebaseFirestore.getInstance() }
    val meuUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    DisposableEffect(Unit) {
        val registro = db.collection("notificacoes")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                listaNotificacoes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Notificacao::class.java)?.copy(id = doc.id)
                        ?.takeIf { it.destinatarioId == "TODOS" || it.destinatarioId == meuUserId }
                }
                isLoading = false
            }
        onDispose { registro.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold, color = primaryGreen) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundOffWhite)
            )
        },
        containerColor = backgroundOffWhite
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(color = primaryGreen, modifier = Modifier.align(Alignment.Center))
                listaNotificacoes.isEmpty() -> Box(modifier = Modifier.align(Alignment.Center)) {
                    EstadoVazio(
                        titulo = "Nenhuma notificação ainda",
                        mensagem = "Quando alguém encontrar o seu pet ou houver um alerta perto de você, ele aparece aqui.",
                        textoBotao = null
                    )
                }
                else -> ListaNotificacoes(listaNotificacoes, meuUserId, visitaAnterior)
            }
        }
    }
}

@Composable
fun ListaNotificacoes(lista: List<Notificacao>, meuUserId: String, visitaAnterior: Long) {
    val sobreMeuPet = lista.filter { it.destinatarioId == meuUserId && meuUserId.isNotEmpty() }
    val comunidade = lista.filter { it.destinatarioId == "TODOS" }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (sobreMeuPet.isNotEmpty()) {
            item { TituloSecaoNotificacoes("SOBRE O SEU PET") }
            items(sobreMeuPet, key = { it.id }) { notificacao ->
                NotificacaoCard(notificacao, privada = true, nova = notificacao.data > visitaAnterior)
            }
        }
        if (comunidade.isNotEmpty()) {
            item { TituloSecaoNotificacoes("ALERTAS DA COMUNIDADE") }
            items(comunidade, key = { it.id }) { notificacao ->
                NotificacaoCard(notificacao, privada = false, nova = notificacao.data > visitaAnterior)
            }
        }
    }
}

@Composable
private fun TituloSecaoNotificacoes(texto: String) {
    Text(
        texto,
        color = Color(0xFF9E9E9E),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
fun NotificacaoCard(notificacao: Notificacao, privada: Boolean, nova: Boolean) {
    val context = LocalContext.current
    val primaryGreen = colorResource(id = R.color.primary_green)
    val formatador = remember { SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR")) }
    val dataFormatada = formatador.format(Date(notificacao.data))
    val telefone = if (privada) telefoneDaNotificacao(notificacao) else ""

    val corDestaque = if (privada) Color(0xFFB06000) else primaryGreen
    val corFundoIcone = if (privada) Color(0xFFFEF7E0) else Color(0xFFE6F4EA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (privada) BorderStroke(1.5.dp, Color(0xFFF2D58A)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(44.dp).background(corFundoIcone, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (privada) Icons.Filled.Pets else Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = corDestaque,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notificacao.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1C2723),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (nova) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFC53929), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "Novo",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notificacao.mensagem, fontSize = 14.sp, color = Color(0xFF4A5550), lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = dataFormatada, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                }
            }

            if (telefone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        abrirWhatsAppComMensagem(
                            context,
                            telefone,
                            "Olá! Recebi o aviso do HelpPet de que você encontrou o meu pet. Muito obrigado! Podemos combinar?"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Falar com quem encontrou", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
