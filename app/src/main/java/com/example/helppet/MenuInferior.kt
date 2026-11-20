package com.example.helppet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ALTURA_FAIXA_ICONE = 40.dp
private val ESPACO_ICONE_TEXTO = 4.dp
private val ALTURA_TEXTO = 16.dp
private val TAMANHO_BOTAO_ADICIONAR = 46.dp

@Composable
fun MenuInferior(telaAtual: String, onAddClick: () -> Unit = {}) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val context = LocalContext.current

    fun abrir(tela: String, destino: Class<*>) {
        if (telaAtual != tela) {
            val intent = Intent(context, destino)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }
    }

    Surface(color = Color.White, shadowElevation = 12.dp) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    iconId = R.drawable.ic_nav_home,
                    label = "Início",
                    selecionado = telaAtual == "Inicio",
                    modifier = Modifier.weight(1f),
                    onClick = { abrir("Inicio", MainActivity::class.java) }
                )
                BottomNavItem(
                    iconId = R.drawable.ic_nav_pets,
                    label = "Animais",
                    selecionado = telaAtual == "Animais",
                    modifier = Modifier.weight(1f),
                    onClick = { abrir("Animais", AnimaisActivity::class.java) }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(TAMANHO_BOTAO_ADICIONAR)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(primaryGreen)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Adicionar",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                BottomNavItem(
                    iconId = R.drawable.ic_nav_map,
                    label = "Mapa",
                    selecionado = telaAtual == "Mapa",
                    modifier = Modifier.weight(1f),
                    onClick = { abrir("Mapa", MapaActivity::class.java) }
                )
                BottomNavItem(
                    iconId = R.drawable.ic_nav_profile,
                    label = "Perfil",
                    selecionado = telaAtual == "Perfil",
                    modifier = Modifier.weight(1f),
                    onClick = { abrir("Perfil", PerfilActivity::class.java) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    iconId: Int,
    label: String,
    selecionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val cor = if (selecionado) primaryGreen else textSecondary

    ItemMenuInferior(label = label, cor = cor, negrito = selecionado, modifier = modifier, onClick = onClick) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selecionado) primaryGreen.copy(alpha = 0.12f) else Color.Transparent)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = label,
                tint = cor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ItemMenuInferior(
    label: String,
    cor: Color,
    negrito: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icone: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.height(ALTURA_FAIXA_ICONE), contentAlignment = Alignment.Center) {
            icone()
        }
        Spacer(modifier = Modifier.height(ESPACO_ICONE_TEXTO))
        Box(modifier = Modifier.height(ALTURA_TEXTO), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = cor,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (negrito) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
