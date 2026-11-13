package com.example.helppet

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import kotlin.random.Random

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

    // Busca em tempo real os animais cadastrados no Firebase
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

                        // Centro do Recife
                        controller.setCenter(GeoPoint(-8.0578, -34.8829))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()


                    // Filtra para manter APENAS os pets que contêm "Perdido" ou "Desaparecido"
                    val listaParaExibir = if (animaisNoBanco.isNotEmpty()) {
                        animaisNoBanco.filter { animal ->
                            animal.nome.contains("Perdido", ignoreCase = true) ||
                                    animal.nome.contains("Desaparecido", ignoreCase = true)
                        }
                    } else {
                        // Se o banco estiver vazio, mostra apenas o exemplo de perdido
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

                    listaParaExibir.forEachIndexed { index, animal ->
                        val ponto = obterCoordenadasRecife(animal.descricao, animal.id.hashCode())
                        val isPerdido = animal.nome.contains("Perdido", ignoreCase = true) ||
                                animal.nome.contains("Desaparecido", ignoreCase = true)

                        // 1. DESENHA O CÍRCULO DA REGIÃO PRÓXIMA (Raio de 450 metros)
                        val circuloRegiao = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(ponto, 450.0)
                            infoWindow = null
                            if (isPerdido) {
                                // Vermelho suave para área de busca de pet perdido
                                fillPaint.color = AndroidColor.argb(45, 197, 57, 41)
                                outlinePaint.color = AndroidColor.rgb(197, 57, 41)
                            } else {
                                // Verde suave para pet encontrado / adoção
                                fillPaint.color = AndroidColor.argb(45, 46, 125, 50)
                                outlinePaint.color = AndroidColor.rgb(46, 125, 50)
                            }
                            outlinePaint.strokeWidth = 3f
                        }
                        mapView.overlays.add(circuloRegiao)

                        // 2. ADICIONA O PINO NO CENTRO DO CÍRCULO
                        val marcador = Marker(mapView).apply {
                            position = ponto
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = animal.nome
                            snippet = "${animal.descricao.ifEmpty { "Recife, PE" }} (Raio aprox. de 450m)"

                            setOnMarkerClickListener { marker, _ ->
                                if (marker.isInfoWindowShown && animal.id !in listOf("demo1", "demo2", "demo3")) {
                                    val intent = Intent(context, DetalhesPetActivity::class.java).apply {
                                        putExtra("nome", animal.nome)
                                        putExtra("especie", animal.especie)
                                        putExtra("raca", animal.raca)
                                        putExtra("genero", animal.genero)
                                        putExtra("idade", animal.idade)
                                        putExtra("fotoUrl", animal.fotoUrl)
                                        putExtra("descricao", animal.descricao)
                                        putExtra("telefone", animal.telefoneContato)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    marker.showInfoWindow()
                                }
                                true
                            }
                        }
                        mapView.overlays.add(marcador)
                        if (index == 0) marcador.showInfoWindow()
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Identifica o bairro na descrição e devolve a coordenada correspondente em Recife
private fun obterCoordenadasRecife(descricao: String, seed: Int): GeoPoint {
    val texto = descricao.lowercase()
    val base = when {
        texto.contains("boa viagem") || texto.contains("pina") -> GeoPoint(-8.1180, -34.8970)
        texto.contains("coelhos") || texto.contains("ilha do leite") -> GeoPoint(-8.0625, -34.8850)
        texto.contains("boa vista") || texto.contains("conde") || texto.contains("derby") -> GeoPoint(-8.0500, -34.8800)
        texto.contains("marco zero") || texto.contains("recife antigo") -> GeoPoint(-8.0631, -34.8711)
        texto.contains("graças") || texto.contains("espinheiro") || texto.contains("aflitos") -> GeoPoint(-8.0440, -34.8970)
        texto.contains("casa amarela") || texto.contains("parnamirim") || texto.contains("jaqueira") -> GeoPoint(-8.0300, -34.9120)
        texto.contains("várzea") || texto.contains("ufpe") || texto.contains("caxangá") -> GeoPoint(-8.0500, -34.9500)
        texto.contains("afogados") || texto.contains("san martin") || texto.contains("bongi") -> GeoPoint(-8.0750, -34.9150)
        texto.contains("imbiribeira") || texto.contains("ipsep") -> GeoPoint(-8.1050, -34.9150)
        texto.contains("olinda") -> GeoPoint(-8.0100, -34.8550)
        else -> GeoPoint(-8.0578, -34.8829)
    }
    val random = Random(seed)
    val latOffset = (random.nextDouble() - 0.5) * 0.003
    val lonOffset = (random.nextDouble() - 0.5) * 0.003
    return GeoPoint(base.latitude + latOffset, base.longitude + lonOffset)
}