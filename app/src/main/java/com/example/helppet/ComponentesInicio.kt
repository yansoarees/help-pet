package com.example.helppet

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

enum class StatusPet(val rotulo: String, val aba: String, val atalho: String, val corFundo: Color, val corTexto: Color) {
    PERDIDO("Desaparecido", "Desaparecidos", "Perdidos", Color(0xFFFCE8E6), Color(0xFFC53929)),
    ADOCAO("Para adoção", "Adoção", "Adoção", Color(0xFFFEF7E0), Color(0xFFB06000)),
    ENCONTRADO("Encontrado", "Achados", "Achados", Color(0xFFE6F4EA), Color(0xFF1B4D3E))
}

fun statusDoAnimal(animal: Animal): StatusPet {
    val nome = animal.nome
    val descricao = animal.descricao.trim()
    return when {
        nome.contains("Perdido", ignoreCase = true) || nome.contains("Desaparecido", ignoreCase = true) -> StatusPet.PERDIDO
        nome.contains("Para Adoção", ignoreCase = true) -> StatusPet.ADOCAO
        nome.contains("Encontrado", ignoreCase = true) || nome.contains("Está comigo", ignoreCase = true) ||
            nome.contains("Avistei na rua", ignoreCase = true) -> StatusPet.ENCONTRADO
        descricao.startsWith("Visto em:", ignoreCase = true) -> StatusPet.PERDIDO
        descricao.startsWith("Saúde:", ignoreCase = true) -> StatusPet.ADOCAO
        else -> StatusPet.ENCONTRADO
    }
}

fun nomeDeExibicao(animal: Animal): String {
    val limpo = animal.nome
        .replace(" (Para Adoção)", "", ignoreCase = true)
        .replace(" (Perdido)", "", ignoreCase = true)
        .replace(" (Desaparecido)", "", ignoreCase = true)
        .replace(" (Encontrado - Está comigo)", "", ignoreCase = true)
        .replace(" (Encontrado - Avistei na rua)", "", ignoreCase = true)
        .replace(" (Está comigo)", "", ignoreCase = true)
        .replace(" (Avistei na rua)", "", ignoreCase = true)
        .trim()
    return when {
        limpo.equals("Desconhecido", ignoreCase = true) -> "${animal.especie} (resgatado)"
        limpo.isEmpty() -> "Sem nome"
        else -> limpo
    }
}

fun localDoAnimal(animal: Animal): String {
    val d = animal.descricao
    return when {
        d.contains("Encontrado em:") -> d.substringAfter("Encontrado em:").trim()
        d.contains("Visto em:") -> d.substringAfter("Visto em:").substringBefore(".").trim()
        animal.descricao.isNotBlank() && statusDoAnimal(animal) == StatusPet.ENCONTRADO -> d.trim()
        else -> "Recife, PE"
    }.ifBlank { "Recife, PE" }
}

fun abrirDetalhesPet(context: Context, animal: Animal) {
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
}

@Composable
fun BarraBusca(texto: String, aoMudar: (String) -> Unit, modifier: Modifier = Modifier) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    OutlinedTextField(
        value = texto,
        onValueChange = aoMudar,
        placeholder = { Text("Buscar por nome, raça ou bairro", color = Color(0xFF9E9E9E)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF757575)) },
        trailingIcon = if (texto.isNotEmpty()) {
            {
                IconButton(onClick = { aoMudar("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpar busca", tint = Color(0xFF757575))
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryGreen,
            unfocusedBorderColor = Color(0xFFE6E8E7),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = primaryGreen
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CardPetGrade(animal: Animal, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val status = statusDoAnimal(animal)
    val nome = nomeDeExibicao(animal)

    Card(
        onClick = { abrirDetalhesPet(context, animal) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            if (animal.fotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = animal.fotoUrl,
                    contentDescription = nome,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.patalogo),
                    error = painterResource(id = R.drawable.patalogo),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(status.corFundo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        tint = status.corTexto.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(status.corTexto, CircleShape))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(status.rotulo, color = status.corTexto, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = nome,
                color = Color(0xFF1C2723),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val detalhe = listOf(animal.especie, animal.raca)
                .filter { it.isNotBlank() && it != "Desconhecida" && it != "ND" }
                .joinToString(" • ")
            if (detalhe.isNotEmpty()) {
                Text(
                    text = detalhe,
                    color = Color(0xFF757575),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = status.corTexto,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = localDoAnimal(animal),
                    color = Color(0xFF757575),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CardEsqueleto(modifier: Modifier = Modifier) {
    val transicao = rememberInfiniteTransition(label = "esqueleto")
    val alfa by transicao.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )
    val cinza = Color(0xFFE9ECEB)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.alpha(alfa)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(cinza))
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp).background(cinza, RoundedCornerShape(7.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(10.dp).background(cinza, RoundedCornerShape(5.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(10.dp).background(cinza, RoundedCornerShape(5.dp)))
        }
    }
}

@Composable
fun EstadoVazio(titulo: String, mensagem: String, textoBotao: String?, onBotao: () -> Unit = {}) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.size(88.dp).background(primaryGreen.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = primaryGreen.copy(alpha = 0.6f), modifier = Modifier.size(44.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(titulo, color = Color(0xFF1C2723), fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(mensagem, color = Color(0xFF757575), fontSize = 14.sp, textAlign = TextAlign.Center)
        if (textoBotao != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBotao,
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(textoBotao, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun nomeDoUsuario(nomeCadastro: String?): String {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return "Visitante"
    return nomeCadastro?.takeIf { it.isNotBlank() }
        ?: user.displayName?.takeIf { it.isNotBlank() }
        ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Usuário"
}

fun criarPerfilSeFaltar() {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
    val ref = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(user.uid)
    ref.get().addOnSuccessListener { doc ->
        if (!doc.exists() || doc.getString("nome").isNullOrBlank()) {
            ref.set(
                mapOf("nome" to nomeDoUsuario(null), "email" to (user.email ?: "")),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }
}
