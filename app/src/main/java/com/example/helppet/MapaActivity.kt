package com.example.helppet

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.File

class MapaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val osmConf = Configuration.getInstance()
        val cacheDir = File(cacheDir, "osm_clean_tiles_v3")
        cacheDir.mkdirs()
        osmConf.osmdroidBasePath = cacheDir
        osmConf.osmdroidTileCache = cacheDir
        osmConf.userAgentValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

        try {
            SqlTileWriter().purgeCache()
        } catch (_: Exception) {}

        setContent {
            MaterialTheme {
                MapaScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen() {
    val context = LocalContext.current
    val primaryGreen = colorResource(id = R.color.primary_green)
    val backgroundOffWhite = colorResource(id = R.color.background_offwhite)

    val db = remember { FirebaseFirestore.getInstance() }
    var animaisNoBanco by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var grupoSelecionado by remember { mutableStateOf<List<Animal>?>(null) }


    LaunchedEffect(Unit) {
        db.collection("animais").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                animaisNoBanco = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Animal::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Pets", fontWeight = FontWeight.Bold, color = primaryGreen) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = primaryGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        mapViewRef?.tileProvider?.clearTileCache()
                        mapViewRef?.invalidate()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recarregar Mapa",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundOffWhite)
            )
        },
        bottomBar = {
            MenuInferior(
                telaAtual = "Mapa",
                onAddClick = {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                }
            )
        },
        containerColor = backgroundOffWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    val arcGisStreetSource = object : OnlineTileSourceBase(
                        "ArcGIS_World_Street_Map",
                        0,
                        19,
                        256,
                        "",
                        arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/")
                    ) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            val z = MapTileIndex.getZoom(pMapTileIndex)
                            val y = MapTileIndex.getY(pMapTileIndex)
                            val x = MapTileIndex.getX(pMapTileIndex)
                            return "$baseUrl$z/$y/$x"
                        }
                    }

                    MapView(ctx).apply {
                        mapViewRef = this
                        setUseDataConnection(true)
                        setTileSource(arcGisStreetSource)
                        setMultiTouchControls(true)
                        controller.setZoom(14.2)


                        controller.setCenter(GeoPoint(-8.0578, -34.8829))
                    }
                },
                update = { mapView ->
                    val ctxMapa = mapView.context
                    mapView.overlays.clear()



                    val listaParaExibir = if (animaisNoBanco.isNotEmpty()) {
                        animaisNoBanco.filter { animal -> statusDoAnimal(animal) == StatusPet.PERDIDO }
                    } else {
                        listOf(
                            Animal(
                                id = "demo1",
                                nome = "Cachorro Caramelo (Perdido)",
                                especie = "Cachorro",
                                raca = "Vira-lata",
                                descricao = "Visto por último perto da Av. Conde da Boa Vista"
                            )
                        )
                    }

                    listaParaExibir
                        .groupBy { animal -> regiaoDoPet(animal.descricao).first }
                        .forEach { (_, petsDaRegiao) ->
                            val ponto = regiaoDoPet(petsDaRegiao.first().descricao).second

                            val circuloRegiao = Polygon(mapView).apply {
                                points = Polygon.pointsAsCircle(ponto, 450.0)
                                infoWindow = null
                                fillPaint.color = AndroidColor.argb(40, 197, 57, 41)
                                outlinePaint.color = AndroidColor.rgb(197, 57, 41)
                                outlinePaint.strokeWidth = 3f
                            }
                            mapView.overlays.add(circuloRegiao)

                            val marcador = Marker(mapView).apply {
                                position = ponto
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = criarIconeMarcador(ctxMapa, AndroidColor.rgb(197, 57, 41), petsDaRegiao.size)
                                infoWindow = null
                                title = petsDaRegiao.joinToString { nomeDeExibicao(it) }
                                setOnMarkerClickListener { _, _ ->
                                    val reais = petsDaRegiao.filter { it.id !in listOf("demo1", "demo2", "demo3") }
                                    when {
                                        reais.size == 1 -> abrirDetalhesPet(context, reais.first())
                                        reais.size > 1 -> grupoSelecionado = reais
                                    }
                                    true
                                }
                            }
                            mapView.overlays.add(marcador)
                        }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            grupoSelecionado?.let { pets ->
                ModalBottomSheet(
                    onDismissRequest = { grupoSelecionado = null },
                    containerColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                        Text(
                            "${pets.size} pets desaparecidos nesta região",
                            color = Color(0xFF1C2723),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toque em um pet para ver o perfil.", color = Color(0xFF757575), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pets.forEach { pet ->
                                LinhaPetMapa(pet) {
                                    grupoSelecionado = null
                                    abrirDetalhesPet(context, pet)
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFC53929), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pets desaparecidos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C2723))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Toque no pin para ver os pets da região", fontSize = 12.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun criarIconeMarcador(context: Context, cor: Int, quantidade: Int = 1): BitmapDrawable {
    val d = context.resources.displayMetrics.density
    val largura = (56 * d).toInt()
    val altura = (72 * d).toInt()
    val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val tinta = Paint(Paint.ANTI_ALIAS_FLAG)

    val cx = largura / 2f
    val raio = largura / 2f - 3 * d
    val cy = raio + 2 * d

    tinta.color = AndroidColor.argb(50, 0, 0, 0)
    canvas.drawCircle(cx, cy + 2 * d, raio, tinta)

    val ponta = Path().apply {
        moveTo(cx - raio * 0.55f, cy + raio * 0.6f)
        lineTo(cx, altura - 1 * d)
        lineTo(cx + raio * 0.55f, cy + raio * 0.6f)
        close()
    }
    tinta.color = cor
    canvas.drawPath(ponta, tinta)

    tinta.color = AndroidColor.WHITE
    canvas.drawCircle(cx, cy, raio, tinta)
    tinta.color = cor
    canvas.drawCircle(cx, cy, raio - 3 * d, tinta)

    if (quantidade > 1) {
        val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = raio * 0.95f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText(if (quantidade > 99) "99+" else quantidade.toString(), cx, cy - (texto.descent() + texto.ascent()) / 2, texto)
        return BitmapDrawable(context.resources, bitmap)
    }

    ContextCompat.getDrawable(context, R.drawable.ic_nav_pets)?.mutate()?.let { pata ->
        pata.setTint(AndroidColor.WHITE)
        val metade = (raio * 0.8f).toInt()
        pata.setBounds((cx - metade).toInt(), (cy - metade).toInt(), (cx + metade).toInt(), (cy + metade).toInt())
        pata.draw(canvas)
    }

    return BitmapDrawable(context.resources, bitmap)
}


private fun regiaoDoPet(descricao: String): Pair<String, GeoPoint> {
    val texto = descricao.lowercase()
    return when {
        texto.contains("boa viagem") || texto.contains("pina") -> "Boa Viagem" to GeoPoint(-8.1180, -34.8970)
        texto.contains("coelhos") || texto.contains("ilha do leite") -> "Coelhos" to GeoPoint(-8.0625, -34.8850)
        texto.contains("boa vista") || texto.contains("conde") || texto.contains("derby") -> "Boa Vista" to GeoPoint(-8.0500, -34.8800)
        texto.contains("marco zero") || texto.contains("recife antigo") -> "Recife Antigo" to GeoPoint(-8.0631, -34.8711)
        texto.contains("graças") || texto.contains("espinheiro") || texto.contains("aflitos") -> "Graças" to GeoPoint(-8.0440, -34.8970)
        texto.contains("casa amarela") || texto.contains("parnamirim") || texto.contains("jaqueira") -> "Casa Amarela" to GeoPoint(-8.0300, -34.9120)
        texto.contains("várzea") || texto.contains("ufpe") || texto.contains("caxangá") -> "Várzea" to GeoPoint(-8.0500, -34.9500)
        texto.contains("afogados") || texto.contains("san martin") || texto.contains("bongi") -> "Afogados" to GeoPoint(-8.0750, -34.9150)
        texto.contains("imbiribeira") || texto.contains("ipsep") -> "Imbiribeira" to GeoPoint(-8.1050, -34.9150)
        texto.contains("olinda") -> "Olinda" to GeoPoint(-8.0100, -34.8550)
        else -> "Centro" to GeoPoint(-8.0578, -34.8829)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinhaPetMapa(pet: Animal, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFF7F8F8),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFCE8E6)),
                contentAlignment = Alignment.Center
            ) {
                if (pet.fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = pet.fotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = Color(0x80C53929), modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nomeDeExibicao(pet), color = Color(0xFF1C2723), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    listOf(pet.especie, localDoAnimal(pet)).filter { it.isNotBlank() }.joinToString(" • "),
                    color = Color(0xFF757575),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}

