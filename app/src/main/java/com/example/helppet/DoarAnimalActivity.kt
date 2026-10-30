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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class DoarAnimalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { DoarAnimalScreen(onBackClick = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoarAnimalScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() } // <- Autenticação
    val coroutineScope = rememberCoroutineScope()

    var nomePet by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var raca by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }
    var vacinadoCastrado by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { fotoUri = uri; Toast.makeText(context, "Foto escolhida com sucesso!", Toast.LENGTH_SHORT).show() }
    }

    val textDark = Color(0xFF1C2723)
    val textHint = Color(0xFF9E9E9E)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val primaryGreen = colorResource(id = R.color.primary_green)

    Column(modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.background_offwhite)).verticalScroll(rememberScrollState()).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Icon(painter = painterResource(id = R.drawable.ic_arrow_seta), contentDescription = "Voltar", tint = textDark, modifier = Modifier.size(24.dp).clickable { onBackClick() })
            Spacer(modifier = Modifier.width(16.dp))
            Text("Colocar para Adoção", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cadastrar Animal", color = primaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "Preenche os dados com carinho para ajudar este pet a encontrar a família perfeita.", color = textSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        LabelWithStar("Foto bonita do pet ", isMandatory = true)
        val dashPath = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).drawBehind {
            drawRoundRect(color = if (fotoUri == null) Color(0xFF758A82) else primaryGreen, style = Stroke(width = 4f, pathEffect = dashPath), cornerRadius = CornerRadius(24f))
        }.clickable { photoPickerLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painter = painterResource(id = R.drawable.ic_camera_add), contentDescription = null, tint = if (fotoUri == null) Color.Unspecified else primaryGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (fotoUri == null) "Adicionar foto" else "Foto adicionada pronta para envio!", color = if (fotoUri == null) Color(0xFF758A82) else primaryGreen, fontSize = 15.sp, fontWeight = if (fotoUri != null) FontWeight.Bold else FontWeight.Normal)
            }
        }

        LabelWithStar("Nome do pet ", isMandatory = true)
        OutlinedTextField(value = nomePet, onValueChange = { nomePet = it }, placeholder = { Text("Ex: Bob", color = textHint) }, colors = customTextFieldColors(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Espécie ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Cachorro", especie == "Cachorro") { especie = "Cachorro" }
            QuickChip("Gato", especie == "Gato") { especie = "Gato" }
            QuickChip("Outro", especie == "Outro") { especie = "Outro" }
        }

        LabelWithStar("Gênero ", isMandatory = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickChip("Macho", genero == "Macho") { genero = "Macho" }
            QuickChip("Fêmea", genero == "Fêmea") { genero = "Fêmea" }
        }

        LabelWithStar("Raça (Opcional)", isMandatory = false)
        OutlinedTextField(value = raca, onValueChange = { raca = it }, placeholder = { Text("Ex: Vira-lata", color = textHint) }, colors = customTextFieldColors(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("Idade aproximada ", isMandatory = true)
        DropdownCampo(label = "Seleciona a idade...", options = listOf("Filhote (0 a 1 ano)", "Jovem (1 a 3 anos)", "Adulto (3 a 7 anos)", "Idoso (7+ anos)"), selected = idade) { idade = it }

        LabelWithStar("Saúde ", isMandatory = true)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickChip("Vacinado", vacinadoCastrado == "Vacinado", modifier = Modifier.weight(1f)) { vacinadoCastrado = "Vacinado" }
                QuickChip("Castrado", vacinadoCastrado == "Castrado", modifier = Modifier.weight(1f)) { vacinadoCastrado = "Castrado" }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickChip("Ambos", vacinadoCastrado == "Ambos", modifier = Modifier.weight(1f)) { vacinadoCastrado = "Ambos" }
                QuickChip("Nenhum", vacinadoCastrado == "Nenhum", modifier = Modifier.weight(1f)) { vacinadoCastrado = "Nenhum" }
            }
        }

        LabelWithStar("WhatsApp para adoção ", isMandatory = true)
        OutlinedTextField(value = telefone, onValueChange = { newValue -> val digits = newValue.filter { it.isDigit() }; if (digits.length <= 11) telefone = digits }, placeholder = { Text("DDD + Número", color = textHint) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = customTextFieldColors(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(60.dp))

        LabelWithStar("História do pet ", isMandatory = true)
        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, placeholder = { Text("É brincalhão? Dá-se bem com crianças?", color = textHint) }, colors = customTextFieldColors(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 4)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (nomePet.isEmpty() || especie.isEmpty() || genero.isEmpty() || idade.isEmpty() || vacinadoCastrado.isEmpty() || telefone.length < 10) {
                    Toast.makeText(context, "Por favor, preenche os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true

                coroutineScope.launch {
                    var linkDaFoto = ""
                    if (fotoUri != null) {
                        val urlUpload = uploadImageToImgBB(context, fotoUri!!)
                        if (urlUpload != null) linkDaFoto = urlUpload
                        else withContext(Dispatchers.Main) { Toast.makeText(context, "Aviso: Falha ao enviar a foto...", Toast.LENGTH_SHORT).show() }
                    }

                    val userId = auth.currentUser?.uid ?: "" // <- Quem está a doar o pet

                    val petAdocao = Animal(
                        id = "",
                        nome = "$nomePet (Para Adoção)",
                        especie = especie,
                        raca = raca.ifEmpty { "Sem raça definida" },
                        genero = genero,
                        idade = idade,
                        fotoUrl = linkDaFoto,
                        descricao = "Saúde: $vacinadoCastrado. $descricao",
                        telefoneContato = telefone,
                        donoId = userId // <- Salva no Banco!
                    )

                    db.collection("animais").add(petAdocao)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Pet cadastrado para adoção com sucesso!", Toast.LENGTH_LONG).show()
                            onBackClick()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Erro ao salvar os dados", Toast.LENGTH_LONG).show()
                        }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Disponibilizar para Adoção", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

suspend fun uploadImageToImgBB(context: Context, imageUri: Uri): String? {
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
private fun QuickChip(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val bgColor = if (isSelected) primaryGreen else Color.White
    val textColor = if (isSelected) Color.White else Color.DarkGray
    Surface(shape = RoundedCornerShape(24.dp), color = bgColor, border = BorderStroke(1.dp, if (isSelected) primaryGreen else Color(0xFFBDBDBD)), modifier = modifier.clickable { onClick() }) {
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
private fun DropdownCampo(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, placeholder = { Text(label, color = Color(0xFF9E9E9E)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth().height(60.dp), shape = RoundedCornerShape(12.dp), colors = customTextFieldColors())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun customTextFieldColors(textColor: Color = Color(0xFF1C2723)): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(focusedBorderColor = colorResource(id = R.color.primary_green), unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedTextColor = textColor, unfocusedTextColor = textColor)
}