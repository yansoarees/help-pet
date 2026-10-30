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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

class DetalhesPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receber os dados enviados da página anterior
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

@OptIn(ExperimentalLayoutApi::class) // Necessário para o FlowRow (quebra de linha automática nas tags)
@Composable
fun DetalhesPetScreen(
    nome: String, especie: String, raca: String, genero: String,
    idade: String, fotoUrl: String, descricao: String, telefone: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Limpar o nome das tags para sabermos o status
    val isAdocao = nome.contains("(Para Adoção)", ignoreCase = true)
    val isPerdido = nome.contains("(Perdido)", ignoreCase = true)
    val nomeLimpo = nome.replace(" (Para Adoção)", "").replace(" (Perdido)", "").replace(" (Está comigo)", "").replace(" (Avistei na rua)", "")

    val corTagBg = if (isPerdido) Color(0xFFFCE8E6) else if (isAdocao) Color(0xFFFEF7E0) else Color(0xFFE6F4EA)
    val corTagTexto = if (isPerdido) Color(0xFFC53929) else if (isAdocao) Color(0xFFB06000) else Color(0xFF2E5A4A)
    val textoTag = if (isPerdido) "Desaparecido" else if (isAdocao) "Para Adoção" else "Encontrado"

    Scaffold(
        bottomBar = {
            // --- BARRA INFERIOR (WHATSAPP) ---
            Surface(
                color = Color(0xFFF5F7FF), // Fundo azulzinho claro igual ao design
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAdocao) "Interessado em adotar esse pet? Entre em contato via WhatsApp!" else "Tens informações sobre este pet? Entra em contacto!",
                        color = Color(0xFF5E35B1), // Cor roxa escura
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { abrirWhatsApp(context, telefone, nomeLimpo) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // Verde WhatsApp
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_nav_pets), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White) // Idealmente colocar icone do whats aqui
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar em contato", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- TOPO COM IMAGEM E BOTÃO VOLTAR ---
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                AsyncImage(
                    model = fotoUrl.ifEmpty { R.drawable.ic_launcher_background },
                    contentDescription = "Foto de $nomeLimpo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Botão voltar estilo bolha (igual ao teu design)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, top = 40.dp).clickable { onBackClick() },
                    shadowElevation = 4.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_seta), contentDescription = "Voltar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voltar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- CONTEÚDO PRINCIPAL ---
            Column(modifier = Modifier.padding(24.dp)) {
                // Título e Status
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = nomeLimpo, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2723), modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(16.dp), color = corTagBg) {
                        Text(text = textoTag, color = corTagTexto, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chips de Informações Rápidas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    InfoChipItem(especie)
                    InfoChipItem(genero)
                    InfoChipItem(raca.takeIf { it.isNotBlank() } ?: "ND")
                    InfoChipItem(idade)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Localização (Estática por agora para simular o design)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Recife, PE, Brasil", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sobre
                Text("Sobre", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2723))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = descricao, color = Color.DarkGray, fontSize = 15.sp, lineHeight = 22.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // Características (Extraídas da descrição ou genéricas para ficar bonito como no design)
                Text("Características", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2723))
                Spacer(modifier = Modifier.height(12.dp))

                // FlowRow faz com que os chips desçam de linha automaticamente se não couberem!
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (descricao.contains("Vacinado", ignoreCase = true)) CaracteristicaChip("Vacinado")
                    if (descricao.contains("Castrado", ignoreCase = true)) CaracteristicaChip("Castrado")
                    if (descricao.contains("Manso", ignoreCase = true) || descricao.contains("Dócil", ignoreCase = true)) CaracteristicaChip("Dócil")
                    CaracteristicaChip("Apto para Quintal")
                    CaracteristicaChip("Bom com Crianças")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- FUNÇÃO MÁGICA QUE ABRE O WHATSAPP ---
fun abrirWhatsApp(context: Context, telefone: String, nomePet: String) {
    try {
        // Limpa tudo o que não for número
        val numeroLimpo = telefone.replace("\\D".toRegex(), "")
        val mensagem = "Olá! Vi o anúncio do pet $nomePet no HelpPet e gostava de falar contigo."
        // Cria o link oficial do WhatsApp
        val uri = Uri.parse("https://wa.me/55$numeroLimpo?text=${Uri.encode(mensagem)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Se a pessoa não tiver o WhatsApp instalado
        e.printStackTrace()
    }
}

// --- COMPONENTES VISUAIS (CHIPS) ---
@Composable
fun InfoChipItem(text: String) {
    if (text.isEmpty() || text == "Desconhecida" || text == "ND") return
    Surface(
        shape = RoundedCornerShape(16.dp), // Um pouco mais redondo
        color = Color(0xFFF5F5F5),
        // Removi a altura fixa (Modifier.height) para ele poder esticar!
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(
                text = text,
                color = Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Mantém centrado
            )
        }
    }
}

@Composable
fun CaracteristicaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAF5FF), // Fundo roxo muito claro (estilo do teu design)
        border = BorderStroke(1.dp, Color(0xFFE9D5FF))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = text, color = Color(0xFF7E22CE), fontSize = 13.sp, fontWeight = FontWeight.Medium) // Texto Roxo
        }
    }
}