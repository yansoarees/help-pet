package com.example.helppet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class PerdiAnimalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PerdiAnimalScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerdiAnimalScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var nomePet by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    // Estado extra para guardar o texto quando a pessoa seleciona "Outro"
    var especieOutroTexto by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var raca by remember { mutableStateOf("") }
    var caracteristicas by remember { mutableStateOf("") }
    var localVisto by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
            Toast.makeText(context, "Foto escolhida com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    val textDark = Color(0xFF1C2723)
    val textHint = Color(0xFF9E9E9E)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val accentRed = Color(0xFFC53929)

    Column(
        modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.background_offwhite)).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Icon(painter = painterResource(id = R.drawable.ic_arrow_seta), contentDescription = "Voltar", tint = textDark, modifier = Modifier.size(24.dp).clickable { onBackClick() })
            Spacer(modifier = Modifier.width(16.dp))
            Text("Perdi um animal", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cadastrar Pet Perdido", color = accentRed, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "Dá o máximo de detalhes possível para ajudar na busca.", color = textSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        LabelWithStar("Foto nítida do pet ", isMandatory = true)
        val dashPath = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp).drawBehind {
                drawRoundRect(color = if (fotoUri == null) Color(0xFF758A82) else accentRed, style = Stroke(width = 4f, pathEffect = dashPath), cornerRadius = CornerRadius(24f))
            }.clickable { photoPickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            // SE TIVER FOTO, MOSTRA A IMAGEM. SE NÃO, MOSTRA O TEXTO/ÍCONE
            if (fotoUri != null) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Foto selecionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(2.dp).clip(RoundedCornerShape(8.dp)) // Padding para não cobrir a borda tracejada
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(id = R.drawable.ic_camera_add), contentDescription = null, tint = Color(0xFF758A82))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Adicionar foto", color = Color(0xFF758A82), fontSize = 15.sp, fontWeight = FontWeight.Normal)
                }
            }
        }

        LabelWithStar("Nome do pet ", isMandatory = true)
        OutlinedTextField(value = nomePet, onValueChange = { nomePet = it }, placeholder = { Text("Ex: Bob", color = textHint) }, colors = customTextFieldColors(accentRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Espécie ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Cachorro", especie == "Cachorro", accentRed) { especie = "Cachorro" }
            QuickChip("Gato", especie == "Gato", accentRed) { especie = "Gato" }
            QuickChip("Outro", especie == "Outro", accentRed) { especie = "Outro" }
        }

        // CAIXA MÁGICA: Só aparece se a espécie escolhida for "Outro"
        if (especie == "Outro") {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = especieOutroTexto,
                onValueChange = { especieOutroTexto = it },
                placeholder = { Text("Qual é a espécie? (Ex: Coelho, Pássaro)", color = textHint) },
                colors = customTextFieldColors(accentRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
        }

        LabelWithStar("Gênero ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Macho", genero == "Macho", accentRed) { genero = "Macho" }
            QuickChip("Fêmea", genero == "Fêmea", accentRed) { genero = "Fêmea" }
        }

        LabelWithStar("Raça (Opcional)", isMandatory = false)
        OutlinedTextField(value = raca, onValueChange = { raca = it }, placeholder = { Text("Ex: Vira-lata", color = textHint) }, colors = customTextFieldColors(accentRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Características e Cor ", isMandatory = true)
        OutlinedTextField(value = caracteristicas, onValueChange = { caracteristicas = it }, placeholder = { Text("Ex: Pelo preto, mancha branca no peito...", color = textHint) }, colors = customTextFieldColors(accentRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Onde foi visto por último? ", isMandatory = true)
        OutlinedTextField(value = localVisto, onValueChange = { localVisto = it }, placeholder = { Text("Bairro, Rua, Referência...", color = textHint) }, colors = customTextFieldColors(accentRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Teu WhatsApp ", isMandatory = true)
        OutlinedTextField(value = telefone, onValueChange = { newValue -> val digits = newValue.filter { it.isDigit() }; if (digits.length <= 11) telefone = digits }, placeholder = { Text("DDD + Número", color = textHint) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = customTextFieldColors(accentRed), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                // Validação de espécie: se for "Outro", o utilizador tem de ter escrito alguma coisa
                val especieFinal = if (especie == "Outro") especieOutroTexto.trim() else especie

                if (!FormValidator.validarNomePet(nomePet) || !FormValidator.validarTelefone(telefone) || especieFinal.isEmpty() || genero.isEmpty() || caracteristicas.isEmpty() || localVisto.isEmpty()) {
                    Toast.makeText(context, "Por favor, preenche os campos obrigatórios corretamente!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true

                coroutineScope.launch {
                    var linkDaFoto = ""
                    if (fotoUri != null) {
                        val urlUpload = uploadImageToImgBBPerdi(context, fotoUri!!)
                        if (urlUpload != null) linkDaFoto = urlUpload
                    }

                    val userId = auth.currentUser?.uid ?: ""

                    val petPerdido = Animal(
                        id = "",
                        nome = "$nomePet (Perdido)",
                        especie = especieFinal, // Salva o nome real da espécie escrita
                        raca = raca.ifEmpty { "Sem raça definida" },
                        genero = genero,
                        idade = "Desconhecida",
                        fotoUrl = linkDaFoto,
                        descricao = "Visto em: $localVisto. Características: $caracteristicas",
                        telefoneContato = telefone,
                        donoId = userId
                    )

                    db.collection("animais").add(petPerdido)
                        .addOnSuccessListener {
                            NotificacaoHelper.enviarAlerta(
                                titulo = "Alerta: Pet Perdido!",
                                mensagem = "$nomePet ($especieFinal) desapareceu perto de $localVisto. Viu este pet?"
                            )
                            isLoading = false
                            Toast.makeText(context, "Pet reportado como perdido com sucesso!", Toast.LENGTH_LONG).show()
                            onBackClick()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Erro ao salvar os dados", Toast.LENGTH_LONG).show()
                        }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = accentRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Registrar Desaparecimento", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private suspend fun uploadImageToImgBBPerdi(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = "f6731151c6f72e4bc18b9ccfed8f032a"
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes == null) return@withContext null
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
            val client = OkHttpClient()
            val formBody = FormBody.Builder().add("image", base64Image).build()
            val request = Request.Builder().url("https://api.imgbb.com/1/upload?key=$apiKey").post(formBody).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) JSONObject(response.body?.string()!!).getJSONObject("data").getString("url") else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}

@Composable
private fun QuickChip(text: String, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    val bgColor = if (isSelected) activeColor else Color.White
    val textColor = if (isSelected) Color.White else Color.DarkGray
    Surface(shape = RoundedCornerShape(24.dp), color = bgColor, border = BorderStroke(1.dp, if (isSelected) activeColor else Color(0xFFBDBDBD)), modifier = Modifier.clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
    }
}

@Composable
private fun LabelWithStar(text: String, isMandatory: Boolean) {
    Row(modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)) {
        Text(text = text, color = Color(0xFF1C2723), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (isMandatory) Text(text = "*", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun customTextFieldColors(activeColor: Color): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(focusedBorderColor = activeColor, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedTextColor = Color(0xFF1C2723), unfocusedTextColor = Color(0xFF1C2723))
}