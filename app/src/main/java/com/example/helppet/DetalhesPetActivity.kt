package com.example.helppet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class DetalhesPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nome = intent.getStringExtra("nome") ?: "Desconhecido"
        val especie = intent.getStringExtra("especie") ?: ""
        val raca = intent.getStringExtra("raca") ?: ""
        val genero = intent.getStringExtra("genero") ?: ""
        val idade = intent.getStringExtra("idade") ?: ""
        val fotoUrl = intent.getStringExtra("fotoUrl") ?: ""
        val descricao = intent.getStringExtra("descricao") ?: "Sem descrição."
        val telefone = intent.getStringExtra("telefone") ?: ""

        setContent {
            MaterialTheme {
                DetalhesPetScreen(
                    nome = nome,
                    especie = especie,
                    raca = raca,
                    genero = genero,
                    idade = idade,
                    fotoUrl = fotoUrl,
                    descricao = descricao,
                    telefone = telefone,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalhesPetScreen(
    nome: String, especie: String, raca: String, genero: String,
    idade: String, fotoUrl: String, descricao: String, telefone: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textDark = Color(0xFF1C2723)

    val statusPet = statusDoAnimal(Animal(nome = nome, descricao = descricao))
    val isAdocao = statusPet == StatusPet.ADOCAO
    val isPerdido = statusPet == StatusPet.PERDIDO
    val nomeLimpoTemp = nome
        .replace(" (Para Adoção)", "", ignoreCase = true)
        .replace(" (Perdido)", "", ignoreCase = true)
        .replace(" (Desaparecido)", "", ignoreCase = true)
        .replace(" (Está comigo)", "", ignoreCase = true)
        .replace(" (Avistei na rua)", "", ignoreCase = true)
        .replace(" (Encontrado - Está comigo)", "", ignoreCase = true)
        .replace(" (Encontrado - Avistei na rua)", "", ignoreCase = true)
        .trim()
    val nomeLimpo = when {
        nomeLimpoTemp.equals("Desconhecido", ignoreCase = true) -> "$especie (Resgatado)"
        nomeLimpoTemp.isEmpty() -> "Sem nome"
        else -> nomeLimpoTemp
    }

    val corTagBg = if (isPerdido) Color(0xFFFCE8E6) else if (isAdocao) Color(0xFFFEF7E0) else Color(0xFFE6F4EA)
    val corTagTexto = if (isPerdido) Color(0xFFC53929) else if (isAdocao) Color(0xFFB06000) else primaryGreen
    val textoTag = if (isPerdido) "Desaparecido" else if (isAdocao) "Para Adoção" else "Encontrado"

    val localizacao = if (descricao.contains("Encontrado em:")) {
        descricao.substringAfter("Encontrado em:").trim()
    } else if (descricao.contains("Visto em:")) {
        descricao.substringAfter("Visto em:").substringBefore(".").trim()
    } else {
        "Recife, PE"
    }

    val informacoes = listOf(
        "Espécie" to especie,
        "Gênero" to genero,
        "Raça" to raca,
        "Idade" to idade
    ).filter { (_, valor) -> valor.isNotBlank() && valor != "Desconhecida" && valor != "ND" }

    val listaCaracteristicas = mutableListOf<String>()
    val descLowerCase = descricao.lowercase()
    if (descLowerCase.contains("vacinad") || descLowerCase.contains("saúde: ambos")) listaCaracteristicas.add("Vacinado")
    if (descLowerCase.contains("castrad") || descLowerCase.contains("saúde: ambos")) listaCaracteristicas.add("Castrado")
    if (descLowerCase.contains("mans") || descLowerCase.contains("dócil") || descLowerCase.contains("docil")) listaCaracteristicas.add("Dócil")
    if (descLowerCase.contains("brincalh")) listaCaracteristicas.add("Brincalhão")
    if (descLowerCase.contains("assustad") || descLowerCase.contains("medros")) listaCaracteristicas.add("Assustado")
    if (descLowerCase.contains("coleira")) listaCaracteristicas.add("Com coleira")
    if (descLowerCase.contains("machucad") || descLowerCase.contains("ferid")) listaCaracteristicas.add("Machucado")

    Scaffold(
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = if (isAdocao) "Gostou do $nomeLimpo? Fale com quem está doando."
                        else "Tem informações sobre este pet? Fale com quem publicou.",
                        color = Color(0xFF757575),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                escopo.launch {
                                    compartilharPetCompleto(
                                        context,
                                        Animal(
                                            nome = nome, especie = especie, raca = raca, genero = genero,
                                            idade = idade, fotoUrl = fotoUrl, descricao = descricao,
                                            telefoneContato = telefone
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Compartilhar", tint = textDark)
                        }
                        Button(
                            onClick = { abrirWhatsApp(context, telefone, nomeLimpo) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                if (isAdocao) "Quero adotar" else "Falar no WhatsApp",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                if (fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = "Foto de $nomeLimpo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(corTagBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = corTagTexto.copy(alpha = 0.45f),
                                modifier = Modifier.size(96.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sem foto", color = corTagTexto.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent)))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BotaoCircular(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") { onBackClick() }
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-28).dp)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = nomeLimpo,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = corTagBg) {
                        Text(
                            text = textoTag,
                            color = corTagTexto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = corTagTexto, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = localizacao, color = Color(0xFF757575), fontSize = 14.sp)
                }

                if (informacoes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    informacoes.chunked(2).forEach { linha ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            linha.forEach { (rotulo, valor) ->
                                InfoTile(rotulo = rotulo, valor = valor, modifier = Modifier.weight(1f))
                            }
                            if (linha.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Sobre", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = descricao.ifBlank { "Sem descrição." },
                    color = Color(0xFF4A5550),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                if (listaCaracteristicas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Características", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listaCaracteristicas.forEach { tag -> CaracteristicaChip(tag) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoCircular(icone: ImageVector, descricao: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.size(44.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icone, contentDescription = descricao, tint = Color(0xFF1C2723), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun InfoTile(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFFF4F7F5), shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(rotulo, color = Color(0xFF8A938F), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(valor, color = Color(0xFF1C2723), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}

fun abrirWhatsApp(context: Context, telefone: String, nomePet: String) {
    abrirWhatsAppComMensagem(context, telefone, "Olá! Vi o anúncio do pet $nomePet no HelpPet e gostaria de conversar.")
}

fun abrirWhatsAppComMensagem(context: Context, telefone: String, mensagem: String) {
    try {
        val numeroLimpo = telefone.replace("\\D".toRegex(), "")
        val uri = Uri.parse("https://wa.me/55$numeroLimpo?text=${Uri.encode(mensagem)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun partilharPet(context: Context, nome: String, especie: String, local: String, status: String) {
    val textoPartilha = """
        Olha este pet no HelpPet! 🐾
        
        Nome: $nome ($status)
        Espécie: $especie
        Localização: $local
        
        Baixa o HelpPet para ajudar!
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textoPartilha)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar pet via...")
    context.startActivity(shareIntent)
}

@Composable
fun InfoChipItem(text: String) {
    if (text.isEmpty() || text == "Desconhecida" || text == "ND") return
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(
                text = text,
                color = Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CaracteristicaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAF5FF),
        border = BorderStroke(1.dp, Color(0xFFE9D5FF))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = text, color = Color(0xFF7E22CE), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}