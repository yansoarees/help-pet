package com.example.helppet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun abrirTelaPublicado(context: Context, status: StatusPet, nomePet: String, avisouDono: Boolean = false) {
    val intent = Intent(context, PublicadoActivity::class.java).apply {
        putExtra("status", status.name)
        putExtra("nome", nomePet)
        putExtra("avisouDono", avisouDono)
    }
    context.startActivity(intent)
}

class PublicadoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = runCatching { StatusPet.valueOf(intent.getStringExtra("status") ?: "") }
            .getOrDefault(StatusPet.ENCONTRADO)
        val nome = intent.getStringExtra("nome").orEmpty().ifBlank { "O pet" }
        val avisouDono = intent.getBooleanExtra("avisouDono", false)

        setContent {
            MaterialTheme {
                PublicadoScreen(
                    status = status,
                    nomePet = nome,
                    avisouDono = avisouDono,
                    onInicio = {
                        val i = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(i)
                        finish()
                    },
                    onCompartilhar = {
                        val texto = "Ajude este pet no HelpPet! 🐾\n\n$nome — ${status.rotulo}\n\nBaixe o HelpPet para ajudar!"
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, texto)
                        }
                        startActivity(Intent.createChooser(envio, "Compartilhar anúncio"))
                    }
                )
            }
        }
    }
}

@Composable
fun PublicadoScreen(
    status: StatusPet,
    nomePet: String,
    avisouDono: Boolean,
    onInicio: () -> Unit,
    onCompartilhar: () -> Unit
) {
    val primaryGreen = colorResource(id = R.color.primary_green)

    val (titulo, mensagem) = when {
        avisouDono -> "Obrigado por ajudar!" to
            "Avisamos o dono de $nomePet. Ele vai receber seu contato e poderá falar com você pelo WhatsApp."
        status == StatusPet.PERDIDO -> "Seu alerta está no ar!" to
            "A comunidade já pode ver $nomePet. Compartilhe o anúncio para mais pessoas ajudarem na busca."
        status == StatusPet.ADOCAO -> "Anúncio publicado!" to
            "$nomePet já aparece para quem quer adotar. Quem se interessar vai falar com você pelo WhatsApp."
        else -> "Obrigado por ajudar!" to
            "Seu alerta foi enviado. Se alguém reconhecer o pet, vai entrar em contato com você."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_offwhite))
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(120.dp).background(status.corFundo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(76.dp).background(status.corTexto, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(titulo, color = Color(0xFF1C2723), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            mensagem,
            color = Color(0xFF616161),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCompartilhar,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compartilhar anúncio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onInicio,
            border = BorderStroke(1.dp, primaryGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Voltar para o início", color = primaryGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
