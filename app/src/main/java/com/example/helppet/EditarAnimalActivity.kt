package com.example.helppet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class EditarAnimalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val petId = intent.getStringExtra("petId") ?: ""
        val nomeAtual = intent.getStringExtra("nome") ?: ""
        val especieAtual = intent.getStringExtra("especie") ?: ""
        val racaAtual = intent.getStringExtra("raca") ?: ""
        val descricaoAtual = intent.getStringExtra("descricao") ?: ""
        val telefoneAtual = intent.getStringExtra("telefone") ?: ""

        setContent {
            MaterialTheme {
                EditarAnimalScreen(
                    petId = petId,
                    nomeAtual = nomeAtual,
                    especieAtual = especieAtual,
                    racaAtual = racaAtual,
                    descricaoAtual = descricaoAtual,
                    telefoneAtual = telefoneAtual,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarAnimalScreen(
    petId: String, nomeAtual: String, especieAtual: String,
    racaAtual: String, descricaoAtual: String, telefoneAtual: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    // 1. Descobre e salva a etiqueta original de forma invisível
    val tagOriginal = when {
        nomeAtual.contains("(Para Adoção)", true) -> "(Para Adoção)"
        nomeAtual.contains("(Perdido)", true) -> "(Perdido)"
        nomeAtual.contains("(Desaparecido)", true) -> "(Desaparecido)"
        nomeAtual.contains("(Encontrado - Está comigo)", true) -> "(Encontrado - Está comigo)"
        nomeAtual.contains("(Encontrado - Avistei na rua)", true) -> "(Encontrado - Avistei na rua)"
        nomeAtual.contains("(Está comigo)", true) -> "(Está comigo)"
        nomeAtual.contains("(Avistei na rua)", true) -> "(Avistei na rua)"
        else -> ""
    }

    // 2. Limpa o nome para o utilizador editar de forma simples
    val nomeLimpoAtual = if (tagOriginal.isNotEmpty()) {
        nomeAtual.replace(" $tagOriginal", "", true).trim()
    } else {
        nomeAtual
    }

    // Se for um pet "Desconhecido", deixa a caixa vazia para a pessoa por o nome
    val nomeParaExibir = if (nomeLimpoAtual.equals("Desconhecido", true)) "" else nomeLimpoAtual

    var nome by remember { mutableStateOf(nomeParaExibir) }
    var especie by remember { mutableStateOf(especieAtual) }
    var raca by remember { mutableStateOf(racaAtual) }
    var descricao by remember { mutableStateOf(descricaoAtual) }
    var telefone by remember { mutableStateOf(telefoneAtual) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryGreen = colorResource(id = R.color.primary_green)
    val textDark = Color(0xFF1C2723)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_offwhite))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
            Icon(painter = painterResource(id = R.drawable.ic_arrow_seta), contentDescription = "Voltar", tint = textDark, modifier = Modifier.size(24.dp).clickable { onBackClick() })
            Spacer(modifier = Modifier.width(16.dp))
            Text("Editar Anúncio", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Text("Nome do pet", fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = nome, onValueChange = { nome = it }, placeholder = { Text("Ex: Bob") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

        Text("Espécie", fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = especie, onValueChange = { especie = it }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

        Text("Raça / Porte", fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = raca, onValueChange = { raca = it }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

        Text("WhatsApp de Contato", fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = telefone, onValueChange = { telefone = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

        Text("Descrição / História", fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 32.dp), shape = RoundedCornerShape(12.dp), maxLines = 4, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

        Button(
            onClick = {
                val nomePreenchido = if (nome.isBlank()) "Desconhecido" else nome.trim()
                if (telefone.isBlank()) {
                    Toast.makeText(context, "O telefone é obrigatório!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true

                // 3. Voltar a colar a tag no nome antes de gravar, para o banco de dados não bugar!
                val nomeParaSalvar = if (tagOriginal.isNotEmpty()) "$nomePreenchido $tagOriginal" else nomePreenchido

                val updates = mapOf(
                    "nome" to nomeParaSalvar,
                    "especie" to especie.trim(),
                    "raca" to raca.trim(),
                    "telefoneContato" to telefone.trim(),
                    "descricao" to descricao.trim()
                )

                db.collection("animais").document(petId).update(updates)
                    .addOnSuccessListener {
                        isLoading = false
                        Toast.makeText(context, "Anúncio atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Erro ao atualizar.", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Salvar Alterações", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}