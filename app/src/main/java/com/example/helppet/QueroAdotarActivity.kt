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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

class QueroAdotarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QueroAdotarScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueroAdotarScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var especieDesejada by remember { mutableStateOf("") }
    // NOVO ESTADO: Guarda o texto quando a pessoa escolhe "Qualquer"
    var especieQualquerTexto by remember { mutableStateOf("") }

    var porteIdeal by remember { mutableStateOf("") }
    var tipoMoradia by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var sobreMim by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    // Estados para o "Match" (Lista de animais sugeridos)
    var isPedidoRegistado by remember { mutableStateOf(false) }
    var petsSugeridos by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var isLoadingPets by remember { mutableStateOf(false) }

    val textDark = Color(0xFF1C2723)
    val textHint = Color(0xFF9E9E9E)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val primaryGreen = colorResource(id = R.color.primary_green)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_offwhite))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_seta),
                contentDescription = "Voltar",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() },
                tint = textDark
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Quero Adotar", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SE O PEDIDO JÁ FOI ENVIADO, MOSTRA A TELA DE "MATCH"
        if (isPedidoRegistado) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE6F4EA), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_adotar),
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Perfil Registado!",
                        color = primaryGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Procuramos na nossa base de dados os pets que combinam com o teu perfil.",
                        color = textSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pets sugeridos para ti:", color = textDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingPets) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            } else if (petsSugeridos.isEmpty()) {
                Text(
                    "No momento, não temos pets disponíveis com essas características exatas. Mas não te preocupes, o teu perfil está ativo e ONGs entrarão em contacto!",
                    color = textHint,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            } else {
                petsSugeridos.forEach { pet ->
                    AnimalAdocaoCard(pet = pet, primaryGreen = primaryGreen) {
                        val intent = Intent(context, DetalhesPetActivity::class.java).apply {
                            putExtra("nome", pet.nome)
                            putExtra("especie", pet.especie)
                            putExtra("raca", pet.raca)
                            putExtra("genero", pet.genero)
                            putExtra("idade", pet.idade)
                            putExtra("fotoUrl", pet.fotoUrl)
                            putExtra("descricao", pet.descricao)
                            putExtra("telefone", pet.telefoneContato)
                        }
                        context.startActivity(intent)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onBackClick() },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Voltar para o Início", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

        } else {
            // TELA DE FORMULÁRIO (Ainda não enviado)
            Text("Perfil de Adotante", color = primaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Regista o teu interesse. Vamos ajudar-te a encontrar o teu novo melhor amigo!",
                color = textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            LabelWithStar("Que pet procuras? ", isMandatory = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickChip("Cachorro", especieDesejada == "Cachorro") { especieDesejada = "Cachorro" }
                QuickChip("Gato", especieDesejada == "Gato") { especieDesejada = "Gato" }
                QuickChip("Qualquer", especieDesejada == "Qualquer") { especieDesejada = "Qualquer" }
            }

            // NOVO: Caixinha extra se escolher "Qualquer"
            if (especieDesejada == "Qualquer") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = especieQualquerTexto,
                    onValueChange = { especieQualquerTexto = it },
                    placeholder = { Text("Especifique (Ex: Qualquer um que seja calmo)", color = textHint) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            }

            LabelWithStar("Qual o porte ideal? ", isMandatory = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickChip("Pequeno", porteIdeal == "Pequeno") { porteIdeal = "Pequeno" }
                QuickChip("Médio", porteIdeal == "Médio") { porteIdeal = "Médio" }
                QuickChip("Grande", porteIdeal == "Grande") { porteIdeal = "Grande" }
            }

            LabelWithStar("Onde vives? ", isMandatory = true)
            DropdownCampo(
                label = "Ex: Apartamento, Casa com quintal...",
                options = listOf("Apartamento", "Casa sem quintal", "Casa com quintal"),
                selected = tipoMoradia
            ) { tipoMoradia = it }

            LabelWithStar("O teu WhatsApp ", isMandatory = true)
            OutlinedTextField(
                value = telefone,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.length <= 11) telefone = digits
                },
                placeholder = { Text("DDD + Número", color = textHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )

            LabelWithStar("Por que queres adotar? ", isMandatory = false)
            OutlinedTextField(
                value = sobreMim,
                onValueChange = { sobreMim = it },
                placeholder = { Text("Fala um pouco sobre ti, a tua rotina e quem mora contigo.", color = textHint) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    // Valida a espécie correta
                    val especieFinal = if (especieDesejada == "Qualquer") {
                        if (especieQualquerTexto.isNotBlank()) especieQualquerTexto.trim() else "Qualquer"
                    } else {
                        especieDesejada
                    }

                    if (especieDesejada.isEmpty() || porteIdeal.isEmpty() || tipoMoradia.isEmpty() || telefone.length < 10) {
                        Toast.makeText(context, "Por favor, preenche todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true

                    val pedidoAdocao = hashMapOf(
                        "especie" to especieFinal,
                        "porte" to porteIdeal,
                        "moradia" to tipoMoradia,
                        "telefone" to telefone,
                        "sobre" to sobreMim,
                        "status" to "Ativo"
                    )

                    db.collection("pedidos_adocao")
                        .add(pedidoAdocao)
                        .addOnSuccessListener {
                            // Sucesso! Agora procura os pets compatíveis
                            isPedidoRegistado = true
                            isLoading = false
                            isLoadingPets = true

                            db.collection("animais").get().addOnSuccessListener { snapshot ->
                                val matchPets = snapshot.documents.mapNotNull { doc ->
                                    val nome = doc.getString("nome") ?: ""
                                    val especiePet = doc.getString("especie") ?: ""
                                    val racaOuPorte = doc.getString("raca") ?: ""

                                    // Filtra para mostrar só os disponíveis para adoção
                                    if (nome.contains("Para Adoção", ignoreCase = true)) {
                                        // Regra de Match: Mesma Espécie (ou 'Qualquer' escolhido originalmente) E mesmo Porte na raça
                                        val matchEspecie = especieDesejada == "Qualquer" || especiePet.contains(especieDesejada, ignoreCase = true)
                                        val matchPorte = racaOuPorte.contains(porteIdeal, ignoreCase = true)

                                        if (matchEspecie && matchPorte) {
                                            Animal(
                                                id = doc.id,
                                                nome = nome,
                                                especie = especiePet,
                                                raca = racaOuPorte,
                                                genero = doc.getString("genero") ?: "",
                                                idade = doc.getString("idade") ?: "",
                                                fotoUrl = doc.getString("fotoUrl") ?: "",
                                                descricao = doc.getString("descricao") ?: "",
                                                telefoneContato = doc.getString("telefoneContato") ?: ""
                                            )
                                        } else null
                                    } else null
                                }
                                petsSugeridos = matchPets
                                isLoadingPets = false
                            }.addOnFailureListener {
                                isLoadingPets = false
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Erro ao enviar pedido.", Toast.LENGTH_LONG).show()
                        }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrar Pedido de Adoção", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- CARD PARA OS PETS SUGERIDOS NO MATCH ---
@Composable
private fun AnimalAdocaoCard(pet: Animal, primaryGreen: Color, onClick: () -> Unit) {
    val nomeLimpo = pet.nome.replace("(Para Adoção)", "", ignoreCase = true).trim()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (pet.fotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = pet.fotoUrl,
                    contentDescription = nomeLimpo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(70.dp).background(Color(0xFFFDECEA), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = primaryGreen)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nomeLimpo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1C2723))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${pet.especie} • ${pet.raca}", fontSize = 13.sp, color = Color(0xFF4A5550))
            }
        }
    }
}

// --- COMPONENTES REUTILIZÁVEIS ---

@Composable
private fun QuickChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val bgColor = if (isSelected) primaryGreen else Color.White
    val textColor = if (isSelected) Color.White else Color.DarkGray

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (isSelected) primaryGreen else Color(0xFFBDBDBD)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun LabelWithStar(text: String, isMandatory: Boolean) {
    Row(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
        Text(text = text, color = Color(0xFF1C2723), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (isMandatory) {
            Text(text = "*", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownCampo(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(label, color = Color(0xFF9E9E9E)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = customTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun customTextFieldColors(textColor: Color = Color(0xFF1C2723)): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colorResource(id = R.color.primary_green),
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor
    )
}