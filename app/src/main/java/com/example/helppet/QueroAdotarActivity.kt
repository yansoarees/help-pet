package com.example.helppet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Variáveis de Estado para o Perfil de Adoção
    var especieDesejada by remember { mutableStateOf("") }
    var porteIdeal by remember { mutableStateOf("") }
    var tipoMoradia by remember { mutableStateOf("") }

    var telefone by remember { mutableStateOf("") }
    var sobreMim by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
        // --- CABEÇALHO ---
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

        Text(
            text = "Perfil de Adotante",
            color = primaryGreen,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Regista o teu interesse. Vamos ajudar-te a encontrar o teu novo melhor amigo!",
            color = textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // --- ESPÉCIE DESEJADA (Chips) ---
        LabelWithStar("Que pet procuras? ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Cachorro", especieDesejada == "Cachorro") { especieDesejada = "Cachorro" }
            QuickChip("Gato", especieDesejada == "Gato") { especieDesejada = "Gato" }
            QuickChip("Qualquer", especieDesejada == "Qualquer") { especieDesejada = "Qualquer" }
        }

        // --- PORTE (Chips) ---
        LabelWithStar("Qual o porte ideal? ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Pequeno", porteIdeal == "Pequeno") { porteIdeal = "Pequeno" }
            QuickChip("Médio", porteIdeal == "Médio") { porteIdeal = "Médio" }
            QuickChip("Grande", porteIdeal == "Grande") { porteIdeal = "Grande" }
        }

        // --- MORADIA (Dropdown Premium que já criámos) ---
        LabelWithStar("Onde vives? ", isMandatory = true)
        DropdownCampo(
            label = "Ex: Apartamento, Casa com quintal...",
            options = listOf("Apartamento", "Casa sem quintal", "Casa com quintal"),
            selected = tipoMoradia
        ) { tipoMoradia = it }

        // --- CONTACTO ---
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

        // --- SOBRE A FAMÍLIA ---
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

        // --- BOTÃO DE REGISTAR INTERESSE ---
        Button(
            onClick = {
                if (especieDesejada.isEmpty() || porteIdeal.isEmpty() || tipoMoradia.isEmpty() || telefone.length < 10) {
                    Toast.makeText(context, "Por favor, preenche todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true

                // Guardar na coleção 'pedidos_adocao' no Firebase
                val pedidoAdocao = hashMapOf(
                    "especie" to especieDesejada,
                    "porte" to porteIdeal,
                    "moradia" to tipoMoradia,
                    "telefone" to telefone,
                    "sobre" to sobreMim,
                    "status" to "Ativo"
                )

                db.collection("pedidos_adocao")
                    .add(pedidoAdocao)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Pedido registado! As ONGs vão ver o teu perfil.", Toast.LENGTH_LONG).show()
                        onBackClick() // Volta para a tela principal
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