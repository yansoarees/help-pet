package com.example.helppet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pega o Token do FCM assim que a app abre
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.d("FCM_TOKEN", "TUA_CHAVE: ${task.result}")
            } else {
                android.util.Log.e("FCM_TOKEN", "Erro ao obter token", task.exception)
            }
        }

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)
    val primaryGreen = colorResource(id = R.color.primary_green)

    val db = remember { FirebaseFirestore.getInstance() }
    var animais by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddMenu by remember { mutableStateOf(false) }

    var categoriaSelecionada by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Cachorros", "Gatos", "Outros")

    LaunchedEffect(Unit) {
        db.collection("animais")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                val lista = snapshot.documents.mapNotNull { document ->
                    document.toObject(Animal::class.java)?.copy(id = document.id)
                }
                animais = lista
                isLoading = false
            }
    }

    val animaisFiltrados = if (categoriaSelecionada == "Todos") {
        animais
    } else {
        animais.filter { animal ->
            animal.especie.equals(categoriaSelecionada.dropLast(1), ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            MenuInferior(
                telaAtual = "Inicio",
                onAddClick = { showAddMenu = true }
            )
        },
        containerColor = backgroundOffWhite
    ) { paddingValues ->

        // Box externa para segurar toda a tela MAIS o pop-up
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)) {
                    HeaderSection()
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categorias) { categoria ->
                        FilterChipCustom(
                            texto = categoria,
                            isSelected = categoria == categoriaSelecionada,
                            onClick = { categoriaSelecionada = categoria },
                            primaryGreen = primaryGreen
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(color = primaryGreen, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (animaisFiltrados.isEmpty()) {
                        Text(
                            text = "Nenhum pet encontrado.",
                            color = colorResource(id = R.color.text_secondary),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp)
                        )
                    } else {
                        animaisFiltrados.forEach { animal ->
                            AnimalCardPremium(animal = animal, primaryGreen = primaryGreen)
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Pop-up com as 4 opções completas!
            if (showAddMenu) {
                Dialog(onDismissRequest = { showAddMenu = false }) {
                    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 8.dp) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("O que você deseja fazer?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                            Spacer(modifier = Modifier.height(24.dp))

                            ActionCard("Perdi um animal", R.drawable.ic_perdi_alert, Color(0xFFFCE8E6), Color(0xFFC53929)) {
                                showAddMenu = false
                                context.startActivity(Intent(context, PerdiAnimalActivity::class.java))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ActionCard("Encontrei um animal", R.drawable.ic_encontrei_pet, Color(0xFFE6F4EA), primaryGreen) {
                                showAddMenu = false
                                context.startActivity(Intent(context, EncontreiAnimalActivity::class.java))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ActionCard("Quero adotar", R.drawable.ic_adotar, Color(0xFFFEF7E0), Color(0xFFB06000)) {
                                showAddMenu = false
                                context.startActivity(Intent(context, QueroAdotarActivity::class.java))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ActionCard("Colocar para Adoção", R.drawable.ic_nav_pets, Color(0xFFF3E5F5), Color(0xFF6A1B9A)) {
                                showAddMenu = false
                                context.startActivity(Intent(context, DoarAnimalActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipCustom(texto: String, isSelected: Boolean, onClick: () -> Unit, primaryGreen: Color) {
    val bgColor = if (isSelected) primaryGreen else Color(0xFFFFFFFF)
    val textColor = if (isSelected) Color.White else Color.DarkGray
    val borderColor = if (isSelected) primaryGreen else Color(0xFFE0E0E0)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = texto,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun AnimalCardPremium(animal: Animal, primaryGreen: Color) {
    val textSecondary = colorResource(id = R.color.text_secondary)
    val context = LocalContext.current

    // Limpeza Mágica para não mostrar "Desconhecido" dos testes antigos
    val nomeLimpoTemp = animal.nome
        .replace(" (Para Adoção)", "", ignoreCase = true)
        .replace(" (Perdido)", "", ignoreCase = true)
        .replace(" (Desaparecido)", "", ignoreCase = true)
        .replace(" (Está comigo)", "", ignoreCase = true)
        .replace(" (Avistei na rua)", "", ignoreCase = true)
        .replace(" (Encontrado - Está comigo)", "", ignoreCase = true)
        .replace(" (Encontrado - Avistei na rua)", "", ignoreCase = true)
        .trim()

    val nomeLimpo = if (nomeLimpoTemp.equals("Desconhecido", ignoreCase = true)) {
        "${animal.especie} (Resgatado)"
    } else if (nomeLimpoTemp.isEmpty()) {
        "Sem Nome"
    } else {
        nomeLimpoTemp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, DetalhesPetActivity::class.java).apply {
                    putExtra("nome", animal.nome)
                    putExtra("especie", animal.especie)
                    putExtra("raca", animal.raca)
                    putExtra("genero", animal.genero)
                    putExtra("idade", animal.idade)
                    putExtra("fotoUrl", animal.fotoUrl)
                    putExtra("descricao", animal.descricao)
                    putExtra("telefone", animal.telefoneContato)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            AsyncImage(
                model = animal.fotoUrl.ifEmpty { "https://via.placeholder.com/600x400?text=Sem+Foto" },
                contentDescription = nomeLimpo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = nomeLimpo,
                        color = colorResource(id = R.color.text_primary),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )

                    val textoTag = when {
                        animal.nome.contains("Perdido", ignoreCase = true) || animal.nome.contains("Desaparecido", ignoreCase = true) -> "Desaparecido"
                        animal.nome.contains("Para Adoção", ignoreCase = true) -> "Para Adoção"
                        else -> "Encontrado"
                    }
                    val corTagBg = when {
                        textoTag == "Desaparecido" -> Color(0xFFFCE8E6)
                        textoTag == "Para Adoção" -> Color(0xFFFEF7E0)
                        else -> Color(0xFFE6F4EA)
                    }
                    val corTagText = when {
                        textoTag == "Desaparecido" -> Color(0xFFC53929)
                        textoTag == "Para Adoção" -> Color(0xFFB06000)
                        else -> primaryGreen
                    }

                    Box(
                        modifier = Modifier.background(corTagBg, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = textoTag, color = corTagText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // A pessoa precisa abrir os Detalhes para partilhar, por isso removi o ícone daqui para forçar a leitura!
                }

                Spacer(modifier = Modifier.height(8.dp))

                val localizacao = if (animal.descricao.contains("Encontrado em:")) {
                    animal.descricao.substringAfter("Encontrado em:").trim()
                } else if (animal.descricao.contains("Visto em:")) {
                    animal.descricao.substringAfter("Visto em:").substringBefore(".").trim()
                } else {
                    "Recife, PE"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Local", tint = textSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = localizacao, color = textSecondary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttributeBadge("Recente")
                    if (animal.raca.isNotEmpty() && animal.raca != "Desconhecida" && animal.raca != "ND") {
                        AttributeBadge(animal.raca)
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeBadge(text: String) {
    Box(
        modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HeaderSection() {
    val context = LocalContext.current
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    var userName by remember { mutableStateOf("...") }
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nomeCompleto = document.getString("nome") ?: "Visitante"
                        val primeiroNome = nomeCompleto.split(" ").firstOrNull() ?: "Visitante"
                        userName = primeiroNome
                    } else {
                        userName = "Visitante"
                    }
                }
                .addOnFailureListener {
                    userName = "Visitante"
                }
        } else {
            userName = "Visitante"
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "HelpPet", color = primaryGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Icon(
                painter = painterResource(id = R.drawable.ic_bell),
                contentDescription = "Notificações",
                tint = primaryGreen,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        context.startActivity(Intent(context, NotificacoesActivity::class.java))
                    }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Olá, $userName!", color = textPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(text = "Juntos podemos ajudar\nmais pets.", color = textSecondary, fontSize = 16.sp)
    }
}

@Composable
fun ActionCard(title: String, iconId: Int, cardBgColor: Color, accentColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(76.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(color = accentColor, shape = CircleShape), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = iconId), contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "›", color = accentColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}