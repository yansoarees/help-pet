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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.text.style.TextOverflow
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

class EncontreiAnimalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { EncontreiAnimalScreen(onBackClick = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncontreiAnimalScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var statusResgate by remember { mutableStateOf("Está comigo") }
    var modoIdentificacao by remember { mutableStateOf("foto") }

    var listaPetsPerdidos by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var isLoadingPerdidos by remember { mutableStateOf(false) }
    var petPerdidoSelecionado by remember { mutableStateOf<Animal?>(null) }

    var especie by remember { mutableStateOf("") }
    var especieOutroTexto by remember { mutableStateOf("") } // NOVO: Para capturar a espécie customizada
    var porte by remember { mutableStateOf("") }
    var corPrincipal by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        isLoadingPerdidos = true
        db.collection("animais").get()
            .addOnSuccessListener { snapshot ->
                val perdidos = snapshot.documents.mapNotNull { doc ->
                    val nome = doc.getString("nome") ?: ""
                    if (nome.contains("Perdido", ignoreCase = true) || nome.contains("Desaparecido", ignoreCase = true)) {
                        Animal(
                            id = doc.id,
                            nome = nome,
                            especie = doc.getString("especie") ?: "",
                            raca = doc.getString("raca") ?: "",
                            genero = doc.getString("genero") ?: "",
                            idade = doc.getString("idade") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: "",
                            descricao = doc.getString("descricao") ?: "",
                            telefoneContato = doc.getString("telefoneContato") ?: "",
                            donoId = doc.getString("donoId") ?: ""
                        )
                    } else null
                }
                listaPetsPerdidos = perdidos
                isLoadingPerdidos = false
            }
            .addOnFailureListener {
                isLoadingPerdidos = false
            }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
            Toast.makeText(context, "Foto adicionada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    val textDark = Color(0xFF1C2723)
    val textHint = Color(0xFF9E9E9E)
    val primaryGreen = colorResource(id = R.color.primary_green)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_offwhite))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_seta),
                contentDescription = "Voltar",
                modifier = Modifier.size(24.dp).clickable { onBackClick() },
                tint = textDark
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Alerta de Pet Encontrado", color = textDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Text("Onde está o animal agora?", color = textDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SelectionCard(
                text = "Está seguro\ncomigo",
                isSelected = statusResgate == "Está comigo",
                modifier = Modifier.weight(1f)
            ) { statusResgate = "Está comigo" }

            SelectionCard(
                text = "Apenas avistei\nna rua",
                isSelected = statusResgate == "Avistei na rua",
                modifier = Modifier.weight(1f)
            ) { statusResgate = "Avistei na rua" }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("Como queres identificar o pet?", color = textDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SelectionCard(
                text = "Tirar foto\n(Pet não listado)",
                isSelected = modoIdentificacao == "foto",
                modifier = Modifier.weight(1f)
            ) {
                modoIdentificacao = "foto"
                petPerdidoSelecionado = null
            }

            SelectionCard(
                text = "Selecionar Pet\n(Lista de Perdidos)",
                isSelected = modoIdentificacao == "lista",
                modifier = Modifier.weight(1f)
            ) {
                modoIdentificacao = "lista"
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (modoIdentificacao == "lista") {
            LabelWithStar("Seleciona o pet desaparecido ", isMandatory = true)

            if (isLoadingPerdidos) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            } else if (listaPetsPerdidos.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum pet cadastrado como perdido no momento.", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Usar modo de Tirar Foto",
                            color = primaryGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { modoIdentificacao = "foto" }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listaPetsPerdidos.forEach { pet ->
                        PetPerdidoItemCard(
                            pet = pet,
                            isSelected = petPerdidoSelecionado?.id == pet.id,
                            primaryGreen = primaryGreen,
                            onClick = {
                                petPerdidoSelecionado = pet
                                especie = pet.especie
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LabelWithStar("Foto atual do animal (Opcional)", isMandatory = false)
            PhotoUploadBox(fotoUri = fotoUri, primaryGreen = primaryGreen) {
                photoPickerLauncher.launch("image/*")
            }
        } else {
            LabelWithStar("Foto do animal ", isMandatory = true)
            PhotoUploadBox(fotoUri = fotoUri, primaryGreen = primaryGreen) {
                photoPickerLauncher.launch("image/*")
            }

            LabelWithStar("Espécie ", isMandatory = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickChip("Cachorro", especie == "Cachorro") { especie = "Cachorro" }
                QuickChip("Gato", especie == "Gato") { especie = "Gato" }
                QuickChip("Outro", especie == "Outro") { especie = "Outro" }
            }

            // NOVO: Caixinha extra que só aparece se escolher "Outro"
            if (especie == "Outro") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = especieOutroTexto,
                    onValueChange = { especieOutroTexto = it },
                    placeholder = { Text("Qual é a espécie? (Ex: Coelho, Pássaro)", color = textHint) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            }

            LabelWithStar("Porte ", isMandatory = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickChip("Pequeno", porte == "Pequeno") { porte = "Pequeno" }
                QuickChip("Médio", porte == "Médio") { porte = "Médio" }
                QuickChip("Grande", porte == "Grande") { porte = "Grande" }
            }

            LabelWithStar("Cor principal ", isMandatory = true)
            OutlinedTextField(
                value = corPrincipal,
                onValueChange = { corPrincipal = it },
                placeholder = { Text("Ex: Preto com manchas brancas", color = textHint) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
        }

        LabelWithStar("Onde foi encontrado? ", isMandatory = true)
        OutlinedTextField(
            value = localizacao,
            onValueChange = { localizacao = it },
            placeholder = { Text("Bairro, rua ou ponto de referência", color = textHint) },
            colors = customTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 3
        )

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

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                // Valida a espécie correta (pega o texto se for "Outro")
                val especieFinal = if (modoIdentificacao == "foto" && especie == "Outro") especieOutroTexto.trim() else especie

                if (modoIdentificacao == "lista") {
                    if (petPerdidoSelecionado == null) {
                        Toast.makeText(context, "Seleciona qual pet da lista encontraste!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (localizacao.isEmpty() || !FormValidator.validarTelefone(telefone)) {
                        Toast.makeText(context, "Preenche o local onde encontraste e um WhatsApp válido!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                } else {
                    if (especieFinal.isEmpty() || porte.isEmpty() || corPrincipal.isEmpty() || localizacao.isEmpty() || !FormValidator.validarTelefone(telefone)) {
                        Toast.makeText(context, "Por favor, preenche as informações rápidas e um telefone válido!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }

                isLoading = true

                coroutineScope.launch {
                    var linkDaFoto = petPerdidoSelecionado?.fotoUrl ?: ""
                    if (fotoUri != null) {
                        val urlUpload = uploadImageToImgBBEncontrei(context, fotoUri!!)
                        if (urlUpload != null) linkDaFoto = urlUpload
                    }

                    val userId = auth.currentUser?.uid ?: ""
                    val petSel = petPerdidoSelecionado

                    if (petSel != null) {
                        val nomeLimpo = petSel.nome.replace("(Perdido)", "", ignoreCase = true)
                            .replace("(Desaparecido)", "", ignoreCase = true).trim()
                        val novoNome = "$nomeLimpo (Encontrado - $statusResgate)"

                        val updates = hashMapOf<String, Any>(
                            "nome" to novoNome,
                            "descricao" to "Encontrado em: $localizacao",
                            "telefoneContato" to telefone,
                            "fotoUrl" to linkDaFoto
                        )

                        db.collection("animais").document(petSel.id).update(updates)
                            .addOnSuccessListener {
                                NotificacaoHelper.enviarAlerta(
                                    titulo = "Notícia sobre $nomeLimpo!",
                                    mensagem = "O pet $nomeLimpo foi marcado como '$statusResgate' perto de $localizacao! Contacto: $telefone"
                                )
                                isLoading = false
                                Toast.makeText(context, "Status atualizado! O pet foi retirado do mapa de buscas.", Toast.LENGTH_LONG).show()
                                onBackClick()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Erro ao atualizar status.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // Evita o nome "Desconhecido": usa a espécie e a cor principal no título!
                        val tituloPet = "$especieFinal $corPrincipal ($statusResgate)"
                        val petEncontrado = Animal(
                            id = "",
                            nome = tituloPet,
                            especie = especieFinal, // Usa a espécie correta
                            raca = "Porte $porte",
                            genero = "",
                            idade = corPrincipal,
                            fotoUrl = linkDaFoto,
                            descricao = localizacao,
                            telefoneContato = telefone,
                            donoId = userId
                        )

                        db.collection("animais").add(petEncontrado)
                            .addOnSuccessListener {
                                NotificacaoHelper.enviarAlerta(
                                    titulo = "Pet Encontrado ($statusResgate)!",
                                    mensagem = "$especieFinal ($corPrincipal, porte $porte) visto em: $localizacao."
                                )
                                isLoading = false
                                Toast.makeText(context, "Alerta enviado! Obrigado por ajudares.", Toast.LENGTH_LONG).show()
                                onBackClick()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Erro ao enviar alerta.", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Enviar Alerta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PetPerdidoItemCard(
    pet: Animal,
    isSelected: Boolean,
    primaryGreen: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) primaryGreen else Color(0xFFE0E0E0)
    val containerColor = if (isSelected) Color(0xFFE6F4EA) else Color.White
    val accentRed = Color(0xFFC53929)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (pet.fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = pet.fotoUrl,
                        contentDescription = pet.nome,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFDECEA), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = accentRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .background(primaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pet.nome.replace("(Perdido)", "", ignoreCase = true)
                            .replace("(Desaparecido)", "", ignoreCase = true).trim(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1C2723)
                    )
                    Surface(
                        color = if (isSelected) primaryGreen else Color(0xFFFDECEA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSelected) "Selecionado" else "Perdido",
                            color = if (isSelected) Color.White else accentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // CORRIGIDO: Bug do traço e quebra de linha que estava a dar erro!
                Text(
                    text = "${pet.especie} • ${pet.raca} ${if (pet.genero.isNotEmpty()) "• ${pet.genero}" else ""}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5550)
                )
                if (pet.descricao.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pet.descricao,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoUploadBox(fotoUri: Uri?, primaryGreen: Color, onClick: () -> Unit) {
    val dashPath = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .drawBehind {
                drawRoundRect(
                    color = if (fotoUri == null) Color(0xFF758A82) else primaryGreen,
                    style = Stroke(width = 4f, pathEffect = dashPath),
                    cornerRadius = CornerRadius(24f)
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // NOVO: Se tiver foto, mostra ela preenchendo a caixa. Se não, mostra o texto.
        if (fotoUri != null) {
            AsyncImage(
                model = fotoUri,
                contentDescription = "Foto pronta",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera_add),
                    contentDescription = null,
                    tint = Color(0xFF758A82)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tirar foto rápida",
                    color = Color(0xFF758A82),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private suspend fun uploadImageToImgBBEncontrei(context: Context, imageUri: Uri): String? {
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
private fun SelectionCard(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val bgColor = if (isSelected) Color(0xFFE6F4EA) else Color.White
    val borderColor = if (isSelected) primaryGreen else Color(0xFFE0E0E0)
    Surface(shape = RoundedCornerShape(16.dp), color = bgColor, border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor), modifier = modifier.height(84.dp).clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) { Text(text = text, color = if (isSelected) primaryGreen else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
    }
}

@Composable
private fun QuickChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val bgColor = if (isSelected) primaryGreen else Color.White
    val textColor = if (isSelected) Color.White else Color.DarkGray
    Surface(shape = RoundedCornerShape(24.dp), color = bgColor, border = BorderStroke(1.dp, if (isSelected) primaryGreen else Color(0xFFBDBDBD)), modifier = Modifier.clickable { onClick() }) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
    }
}

@Composable
private fun LabelWithStar(text: String, isMandatory: Boolean) {
    Row(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
        Text(text = text, color = Color(0xFF1C2723), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (isMandatory) Text(text = "*", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun customTextFieldColors(textColor: Color = Color(0xFF1C2723)): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(focusedBorderColor = colorResource(id = R.color.primary_green), unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedTextColor = textColor, unfocusedTextColor = textColor)
}