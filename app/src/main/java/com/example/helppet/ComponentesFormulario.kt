package com.example.helppet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream

const val PROPORCAO_FOTO = 4f / 3f

private const val IMGBB_API_KEY = "f6731151c6f72e4bc18b9ccfed8f032a"

private fun rotacaoExif(context: Context, uri: Uri): Int = try {
    context.contentResolver.openInputStream(uri)?.use { entrada ->
        when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } ?: 0
} catch (e: Exception) {
    0
}

private fun proporcaoDaFoto(context: Context, uri: Uri): Float? = try {
    val opcoes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
    val girada = rotacaoExif(context, uri) % 180 != 0
    val w = if (girada) opcoes.outHeight else opcoes.outWidth
    val h = if (girada) opcoes.outWidth else opcoes.outHeight
    if (w > 0 && h > 0) w.toFloat() / h else null
} catch (e: Exception) {
    null
}

suspend fun uploadFotoRecortada(context: Context, uri: Uri, vies: Offset): String? = withContext(Dispatchers.IO) {
    try {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= 2048) amostra *= 2
        val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
        var foto = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
            ?: return@withContext null

        val rotacao = rotacaoExif(context, uri)
        if (rotacao != 0) {
            val matriz = Matrix().apply { postRotate(rotacao.toFloat()) }
            foto = Bitmap.createBitmap(foto, 0, 0, foto.width, foto.height, matriz, true)
        }

        val proporcao = foto.width.toFloat() / foto.height
        val (larguraCorte, alturaCorte) = if (proporcao > PROPORCAO_FOTO) {
            (foto.height * PROPORCAO_FOTO).toInt() to foto.height
        } else {
            foto.width to (foto.width / PROPORCAO_FOTO).toInt()
        }
        val x = ((foto.width - larguraCorte) * (vies.x + 1f) / 2f).toInt().coerceIn(0, foto.width - larguraCorte)
        val y = ((foto.height - alturaCorte) * (vies.y + 1f) / 2f).toInt().coerceIn(0, foto.height - alturaCorte)
        var recorte = Bitmap.createBitmap(foto, x, y, larguraCorte, alturaCorte)

        if (recorte.width > 1280) {
            recorte = Bitmap.createScaledBitmap(recorte, 1280, (1280 / PROPORCAO_FOTO).toInt(), true)
        }
        val saida = ByteArrayOutputStream()
        recorte.compress(Bitmap.CompressFormat.JPEG, 85, saida)
        val base64 = Base64.encodeToString(saida.toByteArray(), Base64.DEFAULT)

        val corpo = FormBody.Builder().add("image", base64).build()
        val pedido = Request.Builder().url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY").post(corpo).build()
        OkHttpClient().newCall(pedido).execute().use { resposta ->
            if (!resposta.isSuccessful) return@withContext null
            JSONObject(resposta.body?.string() ?: return@withContext null).getJSONObject("data").getString("url")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CaixaFotoAjustavel(
    fotoUri: Uri?,
    vies: Offset,
    aoMudarVies: (Offset) -> Unit,
    corDestaque: Color,
    aoEscolher: () -> Unit,
    textoVazio: String = "Adicionar foto"
) {
    val context = LocalContext.current
    val proporcaoImagem by produceState<Float?>(initialValue = null, fotoUri) {
        value = fotoUri?.let { withContext(Dispatchers.IO) { proporcaoDaFoto(context, it) } }
    }
    val viesAtual by rememberUpdatedState(vies)
    val aoMudarAtual by rememberUpdatedState(aoMudarVies)

    if (fotoUri == null) {
        val tracejado = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFF758A82),
                        style = Stroke(width = 4f, pathEffect = tracejado),
                        cornerRadius = CornerRadius(40f)
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable { aoEscolher() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color(0xFF758A82), modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(textoVazio, color = Color(0xFF758A82), fontSize = 15.sp)
            }
        }
        return
    }

    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PROPORCAO_FOTO)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEDEFEE))
        ) {
            val larguraPx = constraints.maxWidth.toFloat()
            val alturaPx = constraints.maxHeight.toFloat()
            val r = proporcaoImagem
            val sobraX = if (r != null && r > PROPORCAO_FOTO) alturaPx * r - larguraPx else 0f
            val sobraY = if (r != null && r < PROPORCAO_FOTO) larguraPx / r - alturaPx else 0f

            AsyncImage(
                model = fotoUri,
                contentDescription = "Foto do pet",
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(vies.x, vies.y),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(sobraX, sobraY) {
                        detectDragGestures { mudanca, arrasto ->
                            mudanca.consume()
                            val novoX = if (sobraX > 0f) (viesAtual.x - arrasto.x / (sobraX / 2f)).coerceIn(-1f, 1f) else 0f
                            val novoY = if (sobraY > 0f) (viesAtual.y - arrasto.y / (sobraY / 2f)).coerceIn(-1f, 1f) else 0f
                            aoMudarAtual(Offset(novoX, novoY))
                        }
                    }
            )

            if (sobraX > 0f || sobraY > 0f) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.OpenWith, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arraste para ajustar a foto", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
        TextButton(onClick = aoEscolher, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = corDestaque, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Trocar foto", color = corDestaque, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SeletorEspecie(especie: String, aoMudar: (String) -> Unit, corDestaque: Color) {
    val ehOutra = especie.isNotEmpty() && especie != "Cachorro" && especie != "Gato"
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        PilulaOpcao("Cachorro", especie == "Cachorro", corDestaque) { aoMudar("Cachorro") }
        PilulaOpcao("Gato", especie == "Gato", corDestaque) { aoMudar("Gato") }

        BasicTextField(
            value = if (ehOutra) especie else "",
            onValueChange = { aoMudar(it) },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFF1C2723), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(corDestaque),
            modifier = Modifier.weight(1f),
            decorationBox = { campo ->
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .background(Color.White, RoundedCornerShape(21.dp))
                        .border(
                            BorderStroke(if (ehOutra) 1.5.dp else 1.dp, if (ehOutra) corDestaque else Color(0xFFBDBDBD)),
                            RoundedCornerShape(21.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (!ehOutra) Text("Outra espécie...", color = Color(0xFF9E9E9E), fontSize = 14.sp)
                    campo()
                }
            }
        )
    }
}

@Composable
fun PilulaOpcao(texto: String, selecionado: Boolean, corDestaque: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(if (selecionado) corDestaque else Color.White)
            .border(BorderStroke(1.dp, if (selecionado) corDestaque else Color(0xFFBDBDBD)), RoundedCornerShape(21.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = if (selecionado) Color.White else Color(0xFF424242),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeletorSegmentado(opcoes: List<Pair<String, String>>, selecionado: String, onSelecionar: (String) -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color(0xFFE9EDEB), RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        opcoes.forEach { (valor, rotulo) ->
            val ativo = valor == selecionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (ativo) Modifier.shadow(1.dp, RoundedCornerShape(10.dp)) else Modifier)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (ativo) Color.White else Color.Transparent)
                    .clickable { onSelecionar(valor) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rotulo,
                    color = if (ativo) primaryGreen else Color(0xFF6B7470),
                    fontSize = 14.sp,
                    fontWeight = if (ativo) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

fun formatarTelefone(digitos: String): String = MascaraTelefone.formatar(digitos).first

object MascaraTelefone : androidx.compose.ui.text.input.VisualTransformation {
    fun formatar(digitos: String): Pair<String, IntArray> {
        val n = digitos.length
        val corte = if (n == 11) 5 else 4
        val saida = StringBuilder()
        val origParaForm = IntArray(n + 1)
        for (i in 0 until n) {
            if (i == 0) saida.append('(')
            if (i == 2) saida.append(") ")
            if (i == 2 + corte) saida.append('-')
            origParaForm[i] = saida.length
            saida.append(digitos[i])
        }
        origParaForm[n] = saida.length
        return saida.toString() to origParaForm
    }

    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val (formatado, origParaForm) = formatar(text.text)
        val mapa = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = origParaForm[offset.coerceIn(0, text.length)]
            override fun transformedToOriginal(offset: Int): Int {
                var i = 0
                while (i < text.length && origParaForm[i + 1] <= offset) i++
                return i
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formatado), mapa)
    }
}

@Composable
fun CampoWhatsApp(
    valor: String,
    aoMudar: (String) -> Unit,
    corDestaque: Color,
    preencherDoPerfil: Boolean = true
) {
    val aoMudarAtual by rememberUpdatedState(aoMudar)
    val valorAtual by rememberUpdatedState(valor)

    LaunchedEffect(Unit) {
        if (!preencherDoPerfil || valorAtual.isNotEmpty()) return@LaunchedEffect
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                var digitos = doc.getString("telefone").orEmpty().filter { it.isDigit() }
                if (digitos.length == 13 && digitos.startsWith("55")) digitos = digitos.drop(2)
                if (valorAtual.isEmpty() && digitos.length in 10..11) aoMudarAtual(digitos)
            }
    }

    val completo = valor.length in 10..11
    val incompleto = valor.isNotEmpty() && !completo

    OutlinedTextField(
        value = valor,
        onValueChange = { novo -> aoMudar(novo.filter { it.isDigit() }.take(11)) },
        placeholder = { Text("(81) 99999-9999", color = Color(0xFF9E9E9E)) },
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 14.dp, end = 4.dp)
            ) {
                Text("🇧🇷", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("+55", color = Color(0xFF1C2723), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(12.dp))
                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE0E0E0))
            }
        },
        trailingIcon = if (completo) {
            {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Número completo",
                    tint = Color(0xFF25A35A)
                )
            }
        } else null,
        supportingText = {
            Text(
                when {
                    incompleto -> "Digite o DDD e o número completo"
                    completo -> "As pessoas vão falar com você por este WhatsApp"
                    else -> "Com DDD. Ex: (81) 99999-9999"
                },
                color = if (incompleto) Color(0xFFB06000) else Color(0xFF8A938F),
                fontSize = 12.sp
            )
        },
        visualTransformation = MascaraTelefone,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF1C2723), fontWeight = FontWeight.Medium),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = corDestaque,
            unfocusedBorderColor = if (completo) Color(0xFFCFE7D8) else Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = corDestaque
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
