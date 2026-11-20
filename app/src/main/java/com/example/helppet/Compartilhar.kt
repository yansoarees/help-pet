package com.example.helppet

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

fun textoCompartilhamento(animal: Animal): String {
    val status = statusDoAnimal(animal)
    val cabecalho = when (status) {
        StatusPet.PERDIDO -> "🚨 PET DESAPARECIDO"
        StatusPet.ENCONTRADO -> "🐾 PET ENCONTRADO"
        StatusPet.ADOCAO -> "🏠 PET PARA ADOÇÃO"
    }
    val chamada = when (status) {
        StatusPet.PERDIDO -> "Viu este pet? Fale com o dono:"
        StatusPet.ENCONTRADO -> "É seu ou conhece o dono? Fale com quem encontrou:"
        StatusPet.ADOCAO -> "Quer adotar? Fale com o responsável:"
    }
    val especieRaca = listOf(animal.especie, animal.raca)
        .filter { it.isNotBlank() && it != "Desconhecida" && it != "ND" }
        .joinToString(" • ")
    val telefone = animal.telefoneContato.filter { it.isDigit() }

    return buildString {
        appendLine(cabecalho)
        appendLine()
        appendLine("Nome: ${nomeDeExibicao(animal)}")
        if (especieRaca.isNotEmpty()) appendLine("Espécie: $especieRaca")
        appendLine("Local: ${localDoAnimal(animal)}")
        if (telefone.isNotEmpty()) {
            appendLine()
            appendLine(chamada)
            appendLine("https://wa.me/55$telefone")
        }
        appendLine()
        append("Compartilhado pelo app HelpPet 🐾")
    }
}

private suspend fun baixarFotoParaCompartilhar(context: Context, url: String): Uri? = withContext(Dispatchers.IO) {
    if (url.isBlank()) return@withContext null
    try {
        val resposta = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
        val bytes = resposta.use { if (it.isSuccessful) it.body?.bytes() else null } ?: return@withContext null
        val pasta = File(context.cacheDir, "compartilhar").apply { mkdirs() }
        val arquivo = File(pasta, "pet_${url.hashCode()}.jpg")
        arquivo.writeBytes(bytes)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun compartilharPetCompleto(context: Context, animal: Animal) {
    val texto = textoCompartilhamento(animal)
    val foto = baixarFotoParaCompartilhar(context, animal.fotoUrl)

    val envio = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, texto)
        if (foto != null) {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, foto)
            clipData = ClipData.newRawUri("Foto do pet", foto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }
    context.startActivity(Intent.createChooser(envio, "Compartilhar ${nomeDeExibicao(animal)}"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompartilharPetSheet(animais: List<Animal>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var preparandoId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        ConteudoCompartilhar(
            animais = animais,
            preparandoId = preparandoId,
            onEscolher = { animal ->
                if (preparandoId == null) {
                    preparandoId = animal.id
                    escopo.launch {
                        compartilharPetCompleto(context, animal)
                        preparandoId = null
                        onDismiss()
                    }
                }
            }
        )
    }
}

@Composable
fun ConteudoCompartilhar(animais: List<Animal>, preparandoId: String?, onEscolher: (Animal) -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    var filtro by remember { mutableStateOf<StatusPet?>(null) }
    val lista = animais.filter { filtro == null || statusDoAnimal(it) == filtro }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Compartilhar um pet", color = primaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Escolha o pet para enviar a foto e as informações pelo WhatsApp, Instagram ou outro app.",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipCustom(texto = "Todos", isSelected = filtro == null, onClick = { filtro = null }, primaryGreen = primaryGreen)
            }
            items(StatusPet.entries) { status ->
                FilterChipCustom(
                    texto = status.aba,
                    isSelected = filtro == status,
                    onClick = { filtro = status },
                    primaryGreen = primaryGreen
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (lista.isEmpty()) {
            EstadoVazio("Nenhum pet aqui", "Não há pets nessa categoria para compartilhar.", null)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 460.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lista, key = { it.id.ifEmpty { it.nome } }) { animal ->
                    LinhaCompartilhar(animal, preparando = preparandoId == animal.id) { onEscolher(animal) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinhaCompartilhar(animal: Animal, preparando: Boolean, onClick: () -> Unit) {
    val status = statusDoAnimal(animal)
    val primaryGreen = colorResource(id = R.color.primary_green)

    Surface(
        onClick = onClick,
        color = Color(0xFFF7F8F8),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(status.corFundo),
                contentAlignment = Alignment.Center
            ) {
                if (animal.fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = animal.fotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = status.corTexto.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nomeDeExibicao(animal),
                    color = Color(0xFF1C2723),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(status.corTexto, CircleShape))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "${status.rotulo} • ${localDoAnimal(animal)}",
                        color = Color(0xFF757575),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(40.dp).background(primaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (preparando) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Icon(Icons.Filled.Share, contentDescription = "Compartilhar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
