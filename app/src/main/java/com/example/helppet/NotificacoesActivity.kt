package com.example.helppet

import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notificacao(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val data: Long = System.currentTimeMillis()
)

object NotificacaoHelper {
    fun enviarAlerta(titulo: String, mensagem: String) {
        val db = FirebaseFirestore.getInstance()
        val novaNotificacao = hashMapOf(
            "titulo" to titulo,
            "mensagem" to mensagem,
            "data" to System.currentTimeMillis()
        )
        db.collection("notificacoes").add(novaNotificacao)
    }
}

class NotificacoesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NotificacoesScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(onBackClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)

    var listaNotificacoes by remember { mutableStateOf<List<Notificacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val db = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(Unit) {
        db.collection("notificacoes")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                listaNotificacoes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Notificacao::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas HelpPet", fontWeight = FontWeight.Bold, color = primaryGreen) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundOffWhite)
            )
        },
        containerColor = backgroundOffWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            } else if (listaNotificacoes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum alerta até ao momento.", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(listaNotificacoes) { notificacao ->
                        NotificacaoCard(notificacao, primaryGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacaoCard(notificacao: Notificacao, primaryGreen: Color) {
    val formatador = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val dataFormatada = formatador.format(Date(notificacao.data))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE6F4EA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = primaryGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notificacao.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Text(
                        text = dataFormatada,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = notificacao.mensagem, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}