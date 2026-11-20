package com.example.helppet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAdicionarPet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        ConteudoMenuAdicionar(
            onEscolher = { destino ->
                onDismiss()
                context.startActivity(Intent(context, destino))
            }
        )
    }
}

@Composable
fun ConteudoMenuAdicionar(onEscolher: (Class<*>) -> Unit) {
    val primaryGreen = colorResource(id = R.color.primary_green)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Como você quer ajudar?", color = primaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Escolha uma opção para começar.", color = Color(0xFF757575), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        OpcaoAdicionar(
            titulo = "Perdi um animal",
            subtitulo = "Avise a comunidade e receba ajuda na busca",
            iconeId = R.drawable.ic_perdi_alert,
            corFundo = Color(0xFFFCE8E6),
            corDestaque = Color(0xFFC53929),
            onClick = { onEscolher(PerdiAnimalActivity::class.java) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OpcaoAdicionar(
            titulo = "Encontrei um animal",
            subtitulo = "Ajude um pet a voltar para casa",
            iconeId = R.drawable.ic_encontrei_pet,
            corFundo = Color(0xFFE6F4EA),
            corDestaque = primaryGreen,
            onClick = { onEscolher(EncontreiAnimalActivity::class.java) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OpcaoAdicionar(
            titulo = "Quero adotar",
            subtitulo = "Encontre pets que combinam com você",
            iconeId = R.drawable.ic_adotar,
            corFundo = Color(0xFFFEF7E0),
            corDestaque = Color(0xFFB06000),
            onClick = { onEscolher(QueroAdotarActivity::class.java) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OpcaoAdicionar(
            titulo = "Colocar para adoção",
            subtitulo = "Ajude um pet a encontrar uma família",
            iconeId = R.drawable.ic_nav_pets,
            corFundo = Color(0xFFF3E5F5),
            corDestaque = Color(0xFF6A1B9A),
            onClick = { onEscolher(DoarAnimalActivity::class.java) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpcaoAdicionar(
    titulo: String,
    subtitulo: String,
    iconeId: Int,
    corFundo: Color,
    corDestaque: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = corFundo,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(corDestaque, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconeId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = corDestaque, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitulo, color = Color(0xFF555555), fontSize = 13.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = corDestaque
            )
        }
    }
}
