package com.example.apteczka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apteczka.ui.theme.GreetingCardTheme
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                ApteczkaApp(application = application as MyApplication)
            }
        }
    }
}

// ── Kolory ─────────────────────────────────────────────────────────────────
val GreenLight  = Color(0xFFEAF3DE)
val GreenDark   = Color(0xFF639922)
val GreenText   = Color(0xFF27500A)
val BlueLight   = Color(0xFFE6F1FB)
val BlueDark    = Color(0xFF378ADD)
val BlueText    = Color(0xFF042C53)
val PurpleLight = Color(0xFFEEEDFE)
val PurpleDark  = Color(0xFF7F77DD)
val PurpleText  = Color(0xFF26215C)
val AmberLight  = Color(0xFFFFF8E6)
val AmberText   = Color(0xFF854F0B)

// ── Ekrany ─────────────────────────────────────────────────────────────────
enum class Ekran { GLOWNY, MOJE_LEKI, DODAJ, KATEGORIE, FORMULARZ, WYGASAJACE, WLASNY_LEK, LEKI_KATEGORII, SZCZEGOLY, PRZETERMINOWANE }

// ── Główna aplikacja ───────────────────────────────────────────────────────
@Composable
fun ApteczkaApp(application: MyApplication) {
    var aktualnyEkran by remember { mutableStateOf(Ekran.GLOWNY) }
    var wybranyLek by remember { mutableStateOf<Medicine?>(null) }
    var wybranaKategoriaFiltr by remember { mutableStateOf<String?>(null) }
    var wybranyMojLek by remember { mutableStateOf<UserMedicine?>(null) }

    val dataGraniczna = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, 1)
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
    }
    when (aktualnyEkran) {
        Ekran.GLOWNY -> EkranGlowny(
            onNavigate = { aktualnyEkran = it },
            userDatabase = application.userDatabase
        )
        Ekran.MOJE_LEKI -> MyMedicinesScreen(
            onWstecz = { aktualnyEkran = Ekran.GLOWNY },
            userDatabase = application.userDatabase,
            onLekWybrany = { lek ->
                wybranyMojLek = lek
                aktualnyEkran = Ekran.SZCZEGOLY
            }
        )
        Ekran.DODAJ -> AddMedicineScreen(
            onWstecz = { aktualnyEkran = Ekran.GLOWNY },
            database = application.medicineDatabase,
            onLekWybrany = { lek ->
                wybranyLek = lek
                aktualnyEkran = Ekran.FORMULARZ
            },
            onDodajWlasny = { aktualnyEkran = Ekran.WLASNY_LEK }
        )
        Ekran.FORMULARZ -> wybranyLek?.let { lek ->
            AddMedicineFormScreen(
                lek = lek,
                onWstecz = { aktualnyEkran = Ekran.DODAJ },
                onZapisz = { aktualnyEkran = Ekran.GLOWNY },
                userDatabase = application.userDatabase
            )
        }
        //Ekran.DODAJ      -> EkranDodawania(onWstecz = { aktualnyEkran = Ekran.GLOWNY })
        Ekran.KATEGORIE -> CategoryScreen(
            onWstecz = { aktualnyEkran = Ekran.GLOWNY },
            userDatabase = application.userDatabase,
            onKategoriaWybrana = { nazwa ->
                wybranaKategoriaFiltr = nazwa
                aktualnyEkran = Ekran.LEKI_KATEGORII
            }
        )
        Ekran.WYGASAJACE -> MyMedicinesScreen(
            onWstecz = { aktualnyEkran = Ekran.GLOWNY },
            userDatabase = application.userDatabase,
            tytul = "Wygasające leki",
            filtrData = dataGraniczna
        )

        Ekran.WLASNY_LEK -> AddMedicineFormScreen(
            lek = null,
            onWstecz = { aktualnyEkran = Ekran.DODAJ },
            onZapisz = { aktualnyEkran = Ekran.GLOWNY },
            userDatabase = application.userDatabase
        )

        Ekran.LEKI_KATEGORII -> MyMedicinesScreen(
            onWstecz = { aktualnyEkran = Ekran.KATEGORIE },
            userDatabase = application.userDatabase,
            tytul = wybranaKategoriaFiltr ?: "Kategoria",
            kategoriaFiltr = wybranaKategoriaFiltr
        )

        Ekran.SZCZEGOLY -> wybranyMojLek?.let { lek ->
            MedicineDetailScreen(
                lek = lek,
                onWstecz = { aktualnyEkran = Ekran.MOJE_LEKI },
                onZapisz = { aktualnyEkran = Ekran.MOJE_LEKI },
                userDatabase = application.userDatabase,
                medicineDatabase = application.medicineDatabase
            )
        }

        Ekran.PRZETERMINOWANE -> MyMedicinesScreen(
            onWstecz = { aktualnyEkran = Ekran.GLOWNY },
            userDatabase = application.userDatabase,
            tytul = "Przeterminowane leki",
            filtrPrzeterminowane = true
        )
    }
}

// ── Ekran główny ───────────────────────────────────────────────────────────
@Composable
fun EkranGlowny(onNavigate: (Ekran) -> Unit, userDatabase: UserDatabase) {
    val dzisiaj = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    val dataGraniczna = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, 1)
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
    }
    val wygasajace by userDatabase.userMedicineDao()
        .getWygasajaceLeki(dzisiaj, dataGraniczna)
        .collectAsState(initial = emptyList())

    val przeterminowane by userDatabase.userMedicineDao()
        .getPrzeterminowaneLeki(dzisiaj)
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Tytuł
        Text("\uD83C\uDF3F", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Moja Apteczka",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Kafelki
        MenuKafelek(
            emoji = "📋",
            tytul = "Lista leków",
            opis = "Przeglądaj swoje leki",
            tloKolumna = GreenLight,
            ikonaKolor = GreenDark,
            tekstKolor = GreenText,
            onClick = { onNavigate(Ekran.MOJE_LEKI) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MenuKafelek(
            emoji = "➕",
            tytul = "Dodaj lek",
            opis = "Dodaj nowy lek do apteczki",
            tloKolumna = BlueLight,
            ikonaKolor = BlueDark,
            tekstKolor = BlueText,
            onClick = { onNavigate(Ekran.DODAJ) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MenuKafelek(
            emoji = "🗂️",
            tytul = "Kategorie",
            opis = "Zarządzaj kategoriami",
            tloKolumna = PurpleLight,
            ikonaKolor = PurpleDark,
            tekstKolor = PurpleText,
            onClick = { onNavigate(Ekran.KATEGORIE) }
        )

        Spacer(modifier = Modifier.height(80.dp))

        // Ostrzeżenie
        if (wygasajace.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmberLight)
                    .clickable { onNavigate(Ekran.WYGASAJACE) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${wygasajace.size} ${
                        when {
                            wygasajace.size == 1 -> "lek traci wkrótce ważność"
                            wygasajace.size % 10 in 2..4 && wygasajace.size % 100 !in 12..14 -> "leki tracą wkrótce ważność"
                            else -> "leków traci wkrótce ważność"
                        }
                    }",
                    fontSize = 13.sp,
                    color = AmberText,
                    modifier = Modifier.weight(1f)
                )
                Text("›", fontSize = 22.sp, color = AmberText)
            }
        }

        if (przeterminowane.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFDE7E7))
                    .clickable { onNavigate(Ekran.PRZETERMINOWANE) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚫", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${przeterminowane.size} ${
                        when {
                            przeterminowane.size == 1 -> "lek jest przeterminowany"
                            przeterminowane.size % 10 in 2..4 && przeterminowane.size % 100 !in 12..14 -> "leki są przeterminowane"
                            else -> "leków jest przeterminowanych"
                        }
                    }",
                    fontSize = 13.sp,
                    color = Color(0xFFB71C1C),
                    modifier = Modifier.weight(1f)
                )
                Text("›", fontSize = 22.sp, color = Color(0xFFB71C1C))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ── Kafelek menu ───────────────────────────────────────────────────────────
@Composable
fun MenuKafelek(
    emoji: String,
    tytul: String,
    opis: String,
    tloKolumna: Color,
    ikonaKolor: Color,
    tekstKolor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tloKolumna)
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ikonaKolor),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tytul, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = tekstKolor)
            Text(opis, fontSize = 12.sp, color = ikonaKolor)
        }
        Text("›", fontSize = 22.sp, color = ikonaKolor)
    }
}

// ── Placeholder: Lista leków ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranListy(onWstecz: () -> Unit) {
    BackHandler { onWstecz() } // do powrotu androidowym przyciskiem na poprzendi ekran
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lista leków",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onWstecz) {
                        Text("‹", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                // żeby tytuł był wyśrodkowany mimo ikony wstecz
                actions = {
                    // pusty placeholder tej samej szerokości co navigationIcon
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Tu będzie lista leków 🔜", color = Color.Gray)
        }
    }
}

// ── Placeholder: Dodaj lek ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranDodawania(onWstecz: () -> Unit) {
    BackHandler { onWstecz() } // do powrotu androidowym przyciskiem na poprzendi ekran
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dodaj lek",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onWstecz) {
                        Text("‹", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                // żeby tytuł był wyśrodkowany mimo ikony wstecz
                actions = {
                    // pusty placeholder tej samej szerokości co navigationIcon
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Tu będzie formularz dodawania leku 🔜", color = Color.Gray)
        }
    }
}
// ── Placeholder: Kategorie ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranKategorii(onWstecz: () -> Unit) {
    BackHandler { onWstecz() } // do powrotu androidowym przyciskiem na poprzendi ekran
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kategorie",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onWstecz) {
                        Text("‹", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                // żeby tytuł był wyśrodkowany mimo ikony wstecz
                actions = {
                    // pusty placeholder tej samej szerokości co navigationIcon
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Tu będzie lista kategorii 🔜", color = Color.Gray)
        }
    }
}