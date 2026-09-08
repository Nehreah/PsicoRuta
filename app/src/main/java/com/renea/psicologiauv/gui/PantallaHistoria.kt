package com.renea.psicologiauv.gui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renea.psicologiauv.R

// ========================================================================
// PANTALLA HISTORIA — DISEÑO EDITORIAL INTERACTIVO
// ========================================================================
//
// Rediseño:
// - Jerarquía por secciones numeradas.
// - Exploración histórica con tarjetas compactas y centradas.
// - Conceptos en carrusel horizontal: 3 visibles por página, apilados
//   verticalmente y con el texto completo de cada concepto.
// - Reflexiones en carrusel horizontal: 1 a la vez, con todo el texto visible.
// - Indicadores de desplazamiento coherentes con la identidad visual.
// - No se usan diálogos para leer el contenido: todo se puede leer
//   directamente dentro de la pantalla.
// - Mantiene el contenido editorial original.
// ========================================================================


// ========================================================================
// PALETA
// ========================================================================

private val HistoriaWine = Color(0xFF8F163B)
private val HistoriaWineDark = Color(0xFF5B1029)
private val HistoriaPink = Color(0xFFF8E5EA)
private val HistoriaPinkSoft = Color(0xFFFCF3F5)
private val HistoriaBackground = Color(0xFFFAF9FA)
private val HistoriaText = Color(0xFF281820)
private val HistoriaSecondaryText = Color(0xFF705D66)
private val HistoriaBorder = Color(0xFFE8E2E5)


// ========================================================================
// MODELOS
// ========================================================================

private data class ConceptoHistoria(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private data class ReflexionHistoria(
    val title: String,
    val paragraphs: List<String>
)


// ========================================================================
// PANTALLA PRINCIPAL
// ========================================================================

@Composable
fun PantallaHistoria(
    onVolver: () -> Unit,
    onAbrirCali: () -> Unit,
    onAbrirLatino: () -> Unit
) {

    BackHandler {
        onVolver()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoriaBackground)
    ) {

        HistoriaTopBar(
            onVolver = onVolver
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            HistoriaHero()

            Spacer(modifier = Modifier.height(25.dp))

            // ============================================================
            // 01 — INTRODUCCIÓN
            // ============================================================

            SeccionHistoria(
                numero = "01",
                titulo = "Antes de comenzar..."
            )

            Spacer(modifier = Modifier.height(11.dp))

            IntroduccionCard()

            Spacer(modifier = Modifier.height(27.dp))

            // ============================================================
            // 02 — EXPLORA LA HISTORIA
            // ============================================================

            SeccionHistoria(
                numero = "02",
                titulo = "Explora la historia"
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Recorre dos escenarios fundamentales.",
                style = MaterialTheme.typography.bodySmall,
                color = HistoriaSecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaExploracionPremium(
                imagenId = R.drawable.historia_1,
                imagenDescripcion = "Colombia y Cali",
                kicker = "COLOMBIA / CALI",
                titulo = "Hitos del movimiento estudiantil",
                descripcion =
                    "Acontecimientos, memorias y experiencias que marcaron " +
                            "la movilización estudiantil en Cali y Colombia.",
                onClick = onAbrirCali
            )

            Spacer(modifier = Modifier.height(12.dp))

            TarjetaExploracionPremium(
                imagenId = R.drawable.historia_2,
                imagenDescripcion = "América Latina",
                kicker = "AMÉRICA LATINA",
                titulo = "Una historia de movilización",
                descripcion =
                    "Del movimiento reformista de Córdoba a las nuevas formas " +
                            "de participación y organización estudiantil.",
                onClick = onAbrirLatino
            )

            Spacer(modifier = Modifier.height(27.dp))

            // ============================================================
            // 03 — CONCEPTOS
            // ============================================================

            SeccionHistoria(
                numero = "03",
                titulo = "Conceptos clave"
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Desliza para descubrir las ideas que permiten comprender esta historia.",
                style = MaterialTheme.typography.bodySmall,
                color = HistoriaSecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            CarruselConceptos()

            Spacer(modifier = Modifier.height(27.dp))

            // ============================================================
            // 04 — REFLEXIÓN
            // ============================================================

            SeccionHistoria(
                numero = "04",
                titulo = "Ven a ser parte de esta historia"
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Una reflexión a la vez.",
                style = MaterialTheme.typography.bodySmall,
                color = HistoriaSecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            CarruselReflexion()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


// ========================================================================
// TOP BAR
// ========================================================================

@Composable
private fun HistoriaTopBar(
    onVolver: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HistoriaBackground
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 18.dp,
                    top = 10.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = HistoriaPink
            ) {

                IconButton(
                    onClick = onVolver
                ) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = HistoriaWine
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Historia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HistoriaText
                )

                Text(
                    text = "Movimiento estudiantil",
                    style = MaterialTheme.typography.labelMedium,
                    color = HistoriaSecondaryText
                )
            }

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = HistoriaPinkSoft
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = HistoriaWine,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}


// ========================================================================
// HERO
// ========================================================================

@Composable
private fun HistoriaHero() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = HistoriaWine
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(23.dp)
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(17.dp),
                color = Color.White.copy(alpha = 0.14f)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(17.dp))

            Text(
                text = "Una historia que no muere",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(11.dp))

            Text(
                text =
                    "Hay historias que terminan cuando dejan de contarse. " +
                            "Otras permanecen vivas porque siguen habitando la " +
                            "memoria, el corazón y el espíritu de quienes llegaron después.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = Color.White.copy(alpha = 0.94f)
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text =
                    "La historia del movimiento estudiantil permanece porque " +
                            "sigue habitando nuestras vidas: en las preguntas que " +
                            "heredamos, en las luchas que recordamos y en aquello " +
                            "que todavía nos atrevemos a transformar.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = Color.White.copy(alpha = 0.94f)
            )
        }
    }
}


// ========================================================================
// INTRODUCCIÓN COMPACTA
// ========================================================================

@Composable
private fun IntroduccionCard() {

    var mostrarCompleto by remember {
        mutableStateOf(false)
    }

    val parrafos = listOf(
        "Hablar del movimiento estudiantil es hablar de jóvenes " +
                "que encontraron en la universidad algo más que un lugar " +
                "para estudiar. En ella también encontraron preguntas, " +
                "encuentros, desacuerdos y posibilidades para intervenir " +
                "en la realidad que los rodea.",

        "Las aulas, los pasillos, las asambleas, las calles y los " +
                "espacios de encuentro pueden convertirse en escenarios " +
                "donde una experiencia individual comienza a reconocerse " +
                "como parte de algo colectivo.",

        "Por eso, esta historia no busca únicamente responder qué ocurrió. " +
                "También invita a preguntarnos quiénes fueron esos estudiantes, " +
                "qué los movilizó, cómo construyeron sus vínculos y de qué manera " +
                "esas experiencias transformaron la forma en que se comprenden " +
                "a sí mismos y también al mundo.",

        "Antes de entrar en los conceptos que permiten comprender este fenómeno, " +
                "te invitamos a recorrer dos escenarios fundamentales de esta historia: " +
                "Cali y América Latina."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = if (mostrarCompleto) {
                    parrafos.joinToString("\n\n")
                } else {
                    parrafos.first()
                },
                style = MaterialTheme.typography.bodyMedium,
                color = HistoriaSecondaryText,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (mostrarCompleto) "Mostrar menos ↑" else "Leer introducción completa →",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HistoriaWine,
                modifier = Modifier.clickable {
                    mostrarCompleto = !mostrarCompleto
                }
            )
        }
    }
}


// ========================================================================
// TÍTULOS DE SECCIÓN
// ========================================================================

@Composable
private fun SeccionHistoria(
    numero: String,
    titulo: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(35.dp),
            shape = CircleShape,
            color = HistoriaPink
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = numero,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = HistoriaWine
                )
            }
        }

        Spacer(modifier = Modifier.width(11.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = HistoriaText
        )
    }
}


// ========================================================================
// EXPLORACIÓN HISTÓRICA
// ========================================================================

@Composable
private fun TarjetaExploracionPremium(
    imagenId: Int,
    imagenDescripcion: String,
    kicker: String,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 17.dp,
                vertical = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = imagenId),
                contentDescription = imagenDescripcion,
                modifier = Modifier
                    .width(210.dp)
                    .height(112.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(11.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = HistoriaPink
            ) {

                Text(
                    text = kicker,
                    modifier = Modifier.padding(
                        horizontal = 11.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HistoriaWine
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = HistoriaText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = HistoriaSecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(11.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Toca para explorar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = HistoriaWine
                )

                Spacer(modifier = Modifier.width(5.dp))

                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = HistoriaWine,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


// ========================================================================
// ========================================================================
// CARRUSEL DE CONCEPTOS — 3 VISIBLES, DESLIZAMIENTO HORIZONTAL
// ========================================================================
//
// Cada página contiene exactamente 3 conceptos apilados verticalmente.
// No se recorta el texto: la tarjeta crece según su contenido.
// Para descubrir los siguientes 3, el usuario desliza horizontalmente.
// ========================================================================

@Composable
private fun CarruselConceptos() {

    val conceptos = remember {
        listOf(
            ConceptoHistoria(
                icon = Icons.Filled.Person,
                title = "Juventud",
                description =
                    "La juventud es comprendida como una experiencia social, " +
                            "cultural y política. Más que una condición determinada " +
                            "únicamente por la edad, implica formas particulares de " +
                            "sentir, interpretar y ocupar el mundo."
            ),
            ConceptoHistoria(
                icon = Icons.Filled.Groups,
                title = "Alteractivismo",
                description =
                    "Una forma contemporánea de comprender la acción activista: " +
                            "más horizontal, creativa y situada. El Alteractivismo " +
                            "permite observar cómo surgen nuevas maneras de participar " +
                            "y de imaginar la transformación social."
            ),
            ConceptoHistoria(
                icon = Icons.Filled.Place,
                title = "Espacio vital",
                description =
                    "No se reduce a un lugar geográfico. Se construye en la " +
                            "relación entre espacio, tiempo, experiencias y sujetos. " +
                            "Por eso, la experiencia estudiantil puede extenderse " +
                            "desde la universidad hacia las calles y otros espacios " +
                            "de participación."
            ),
            ConceptoHistoria(
                icon = Icons.Filled.Person,
                title = "Identidad",
                description =
                    "Se construye en relación con uno mismo, con los otros " +
                            "y con el entorno. La experiencia universitaria puede " +
                            "transformar la manera en que el estudiante se reconoce " +
                            "y comprende su lugar dentro de una comunidad."
            ),
            ConceptoHistoria(
                icon = Icons.Filled.Lightbulb,
                title = "Motivaciones",
                description =
                    "Son las razones, experiencias y sentidos que impulsan " +
                            "a una persona hacia la participación y la acción. " +
                            "No existe una única razón para movilizarse: cada " +
                            "trayectoria aporta sus propios motivos."
            ),
            ConceptoHistoria(
                icon = Icons.Filled.Groups,
                title = "Participación y acción colectiva",
                description =
                    "La participación permite pasar de la experiencia " +
                            "individual a la construcción colectiva. A través de " +
                            "ella se comparten sentidos, se construyen vínculos " +
                            "y se buscan posibilidades de transformación."
            )
        )
    }

    val pageCount = (conceptos.size + 2) / 3

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount }
    )

    Column {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 1.dp)
        ) { page ->

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                val inicio = page * 3

                repeat(3) { posicion ->

                    val indice = inicio + posicion

                    if (indice < conceptos.size) {

                        ConceptoCompletoCard(
                            numero = indice + 1,
                            concepto = conceptos[indice]
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        IndicadorCarruselVertical(
            actual = pagerState.currentPage,
            total = pagerState.pageCount
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = if (pagerState.currentPage == 0) {
                "Desliza hacia la izquierda para ver más conceptos"
            } else {
                "Desliza horizontalmente para explorar"
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = HistoriaSecondaryText
        )
    }
}


// ========================================================================
// TARJETA COMPLETA DE CONCEPTO
// ========================================================================

@Composable
private fun ConceptoCompletoCard(
    numero: Int,
    concepto: ConceptoHistoria
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 15.dp,
                    vertical = 13.dp
                ),
            verticalAlignment = Alignment.Top
        ) {

            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = HistoriaPink
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = concepto.icon,
                        contentDescription = null,
                        tint = HistoriaWine,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "0$numero",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HistoriaWine
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = concepto.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = HistoriaText
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = concepto.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HistoriaSecondaryText,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}


// ========================================================================
// CARRUSEL DE REFLEXIONES — UNA A LA VEZ, DESLIZAMIENTO HORIZONTAL
// ========================================================================

@Composable
private fun CarruselReflexion() {

    val reflexiones = remember {
        listOf(
            ReflexionHistoria(
                title = "Más allá de la protesta",
                paragraphs = listOf(
                    "El movimiento estudiantil no puede explicarse únicamente " +
                            "por sus momentos más visibles. Una marcha, un paro, " +
                            "una toma o una protesta son apenas la parte que alcanza " +
                            "a ver quien observa desde afuera.",
                    "Cuando nos quedamos solamente con esas imágenes, corremos " +
                            "el riesgo de invisibilizar las razones de ser del movimiento: " +
                            "a sus estudiantes, sus historias, sus sueños, sus ilusiones, " +
                            "sus desacuerdos, sus fracasos e incluso sus errores. Detrás " +
                            "de cada movilización existen personas que viven una época, " +
                            "que sienten sus tensiones y que intentan encontrar una " +
                            "respuesta frente a aquello que consideran injusto."
                )
            ),
            ReflexionHistoria(
                title = "Donde comienza la necesidad",
                paragraphs = listOf(
                    "A veces olvidamos algo fundamental: las revoluciones no " +
                            "surgen simplemente de la rebeldía. Nacen de la necesidad. " +
                            "De la necesidad de ser escuchado, de recuperar derechos, " +
                            "de reclamar aquello que ha sido negado y de defender " +
                            "una dignidad que, para quienes la viven, no puede seguir " +
                            "esperando."
                )
            ),
            ReflexionHistoria(
                title = "Cuando la historia se cuenta desde un solo lugar",
                paragraphs = listOf(
                    "La historia tampoco siempre se cuenta desde el mismo lugar. " +
                            "Con frecuencia, cuando se escribe desde la mirada de unos pocos, " +
                            "quienes se movilizan terminan reducidos a etiquetas sencillas: " +
                            "rebeldes, agitadores, desadaptados o incluso criminales.",
                    "Pero mirar más de cerca permite encontrar otra realidad. Son " +
                            "movilizaciones que aparecen en momentos concretos de la historia, " +
                            "cuando las condiciones hacen evidente la necesidad de exigir " +
                            "el respeto por los derechos, cuestionar aquello que parece " +
                            "inamovible y reivindicar la dignidad humana."
                )
            ),
            ReflexionHistoria(
                title = "Ser humano también es contradecirse",
                paragraphs = listOf(
                    "Eso no significa idealizar el movimiento ni negar sus contradicciones. " +
                            "Significa reconocer que toda lucha también está hecha de seres humanos: " +
                            "personas que se equivocan, que dudan, que se cansan, que imaginan " +
                            "y que, aun así, deciden encontrarse con otros para intentar cambiar " +
                            "aquello que consideran necesario."
                )
            ),
            ReflexionHistoria(
                title = "Una historia que no muere",
                paragraphs = listOf(
                    "Una historia que no muere no es una historia que permanece intacta. " +
                            "Es una historia que vuelve a nosotros cada vez que una nueva " +
                            "generación pregunta, recuerda, cuestiona y decide qué hacer " +
                            "con el mundo que recibió.",
                    "Tal vez esa sea, finalmente, la fuerza de la memoria: recordarnos " +
                            "que detrás de cada movimiento hubo personas que soñaron con " +
                            "algo distinto. Y que mientras exista la necesidad de defender " +
                            "la dignidad, la historia seguirá teniendo algo que decirnos."
                )
            )
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { reflexiones.size }
    )

    Column {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 1.dp)
        ) { page ->

            ReflexionCompletaCard(
                reflexion = reflexiones[page]
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        IndicadorCarruselVertical(
            actual = pagerState.currentPage,
            total = pagerState.pageCount
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Desliza horizontalmente para continuar",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = HistoriaSecondaryText
        )
    }
}


// ========================================================================
// TARJETA COMPLETA DE REFLEXIÓN
// ========================================================================

@Composable
private fun ReflexionCompletaCard(
    reflexion: ReflexionHistoria
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(
            containerColor = HistoriaPinkSoft
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(21.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = HistoriaPink
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = HistoriaWine,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = reflexion.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HistoriaText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            reflexion.paragraphs.forEachIndexed { index, paragraph ->

                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HistoriaSecondaryText,
                    textAlign = TextAlign.Justify
                )

                if (index != reflexion.paragraphs.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


// ========================================================================
// INDICADOR VERTICAL
// ========================================================================

@Composable
private fun IndicadorCarruselVertical(
    actual: Int,
    total: Int
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        repeat(total) { indice ->

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(
                        width = if (indice == actual) 22.dp else 7.dp,
                        height = 7.dp
                    )
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (indice == actual) {
                            HistoriaWine
                        } else {
                            HistoriaPink
                        }
                    )
            )
        }
    }
}
