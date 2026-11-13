package com.example.helppet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

class AnimaisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AnimaisScreen(
                    onBackClick = { finish() },
                    onHomeClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun AnimaisScreen(onBackClick: () -> Unit, onHomeClick: () -> Unit) {
    val backgroundColor = colorResource(id = R.color.background_offwhite)
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var tabSelecionada by remember { mutableStateOf("Desaparecidos") }
    val abas = listOf("Desaparecidos", "Achados", "Adoção")

    var showAddMenu by remember { mutableStateOf(false) }

    var listaAnimais by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("animais").addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val pets = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Animal::class.java)?.copy(id = doc.id)
                }
                listaAnimais = pets
                isLoading = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            MenuInferior(
                telaAtual = "Animais",
                onAddClick = { showAddMenu = true }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_seta),
                    contentDescription = "Voltar",
                    tint = Color(0xFF1C2723),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() }
                )
                Text(
                    text = "Animais",
                    color = Color(0xFF1C2723),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = "Buscar",
                    tint = Color(0xFF1C2723),
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                abas.forEach { aba ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { tabSelecionada = aba },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = aba,
                            color = if (tabSelecionada == aba) Color(0xFF2E5A4A) else Color(0xFF9E9E9E),
                            fontWeight = if (tabSelecionada == aba) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (tabSelecionada == aba) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(3.dp)
                                    .background(Color(0xFF2E5A4A), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val listaFiltrada = listaAnimais.filter { animal ->
                val isPerdido = animal.nome.contains("Perdido", ignoreCase = true) || animal.nome.contains("Desaparecido", ignoreCase = true)
                val isAdocao = animal.nome.contains("Para Adoção", ignoreCase = true)

                when (tabSelecionada) {
                    "Desaparecidos" -> isPerdido
                    "Achados" -> animal.nome.contains("Encontrado", ignoreCase = true) || animal.nome.contains("Está comigo", ignoreCase = true) || animal.nome.contains("Avistei na rua", ignoreCase = true)
                    "Adoção" -> isAdocao
                    else -> true
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E5A4A))
                }
            } else if (listaFiltrada.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum pet encontrado nesta categoria.", color = Color(0xFF9E9E9E))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(listaFiltrada) { animal ->
                        AnimalItemListCardReal(animal)
                    }
                }
            }
        }

        if (showAddMenu) {
            val primaryGreen = colorResource(id = R.color.primary_green)
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

@Composable
fun AnimalItemListCardReal(animal: Animal) {
    val context = LocalContext.current

    val isPerdido = animal.nome.contains("Perdido", ignoreCase = true) || animal.nome.contains("Desaparecido", ignoreCase = true)
    val isAdocao = animal.nome.contains("Para Adoção", ignoreCase = true)

    val (corTag, textoTag) = when {
        isPerdido -> Color(0xFFD32F2F) to "Desaparecido"
        isAdocao -> Color(0xFFFF9800) to "Adoção"
        else -> colorResource(id = R.color.primary_green) to "Encontrado"
    }

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
    } else {
        nomeLimpoTemp
    }
    Card(
        onClick = {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MOSTRA A FOTO REAL OU UM FUNDO COM ÍCONE CASO NÃO TENHA FOTO
            if (animal.fotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = animal.fotoUrl,
                    contentDescription = nomeLimpo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 100.dp, height = 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 120.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Sem foto",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nomeLimpo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C2723),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(corTag, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = textoTag, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                val infoExtra = if (animal.genero.isNotEmpty()) "${animal.especie} • ${animal.genero}" else animal.especie
                Text(text = infoExtra, fontSize = 12.sp, color = Color(0xFF757575))

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = animal.raca, fontSize = 12.sp, color = Color(0xFF757575), maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = animal.descricao, fontSize = 12.sp, color = Color(0xFF757575), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}