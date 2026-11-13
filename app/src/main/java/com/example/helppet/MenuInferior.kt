package com.example.helppet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun MenuInferior(telaAtual: String, onAddClick: () -> Unit = {}) {
    val primaryGreen = colorResource(id = R.color.primary_green)
    val textSecondary = colorResource(id = R.color.text_secondary)
    val context = LocalContext.current

    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. INÍCIO
            val corInicio = if (telaAtual == "Inicio") primaryGreen else textSecondary
            BottomNavItem(
                iconId = R.drawable.ic_nav_home, label = "Início", tint = corInicio, isBold = telaAtual == "Inicio",
                modifier = Modifier.weight(1f).clickable {
                    if (telaAtual != "Inicio") {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        context.startActivity(intent)
                    }
                }
            )

            // 2. ANIMAIS
            val corAnimais = if (telaAtual == "Animais") primaryGreen else textSecondary
            BottomNavItem(
                iconId = R.drawable.ic_nav_pets, label = "Animais", tint = corAnimais, isBold = telaAtual == "Animais",
                modifier = Modifier.weight(1f).clickable {
                    if (telaAtual != "Animais") {
                        val intent = Intent(context, AnimaisActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        context.startActivity(intent)
                    }
                }
            )

            // 3. BOTÃO CENTRAL (+)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(46.dp).background(primaryGreen, CircleShape).clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 26.sp)
                }
            }

            // 4. MAPA
            val corMapa = if (telaAtual == "Mapa") primaryGreen else textSecondary
            BottomNavItem(
                iconId = R.drawable.ic_nav_map, label = "Mapa", tint = corMapa, isBold = telaAtual == "Mapa",
                modifier = Modifier.weight(1f).clickable {
                    if (telaAtual != "Mapa") {
                        val intent = Intent(context, MapaActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        context.startActivity(intent)
                    }
                }
            )

            // 5. PERFIL
            val corPerfil = if (telaAtual == "Perfil") primaryGreen else textSecondary
            BottomNavItem(
                iconId = R.drawable.ic_nav_profile, label = "Perfil", tint = corPerfil, isBold = telaAtual == "Perfil",
                modifier = Modifier.weight(1f).clickable {
                    if (telaAtual != "Perfil") {
                        val intent = Intent(context, PerfilActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(iconId: Int, label: String, tint: Color, isBold: Boolean = false, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(painter = painterResource(id = iconId), contentDescription = label, tint = tint, modifier = Modifier.size(34.dp))
        Text(text = label, color = tint, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}