package com.example.apteczka

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    lek: UserMedicine,
    onWstecz: () -> Unit,
    onZapisz: () -> Unit,
    userDatabase: UserDatabase,
    medicineDatabase: MedicineDatabase
) {
    var nazwa by remember { mutableStateOf(lek.nazwa) }
    var dataWaznosci by remember { mutableStateOf(lek.dataWaznosci) }
    var ilosc by remember { mutableStateOf(lek.ilosc?.toString() ?: "") }
    var jednostka by remember { mutableStateOf(lek.jednostka ?: "") }
    var wybrane by remember { mutableStateOf(
        lek.kategoria?.split(", ")?.toSet() ?: emptySet()
    ) }
    var uwagi by remember { mutableStateOf(lek.uwagi ?: "") }
    var pokazKalendarz by remember { mutableStateOf(false) }
    var pokazKategorie by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val kategorie by userDatabase.categoryDao().getAllCategories().collectAsState(initial = emptyList())

    // Dane z bazy leków
    var daneBazy by remember { mutableStateOf<Medicine?>(null) }
    LaunchedEffect(lek.medicineId) {
        lek.medicineId?.let { id ->
            daneBazy = medicineDatabase.medicineDao().getLekById(id)
        }
    }

    BackHandler { onWstecz() }

    // Kalendarz
    if (pokazKalendarz) {
        val datePickerState = rememberDatePickerState()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        DatePickerDialog(
            onDismissRequest = { pokazKalendarz = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataWaznosci = formatter.format(java.util.Date(millis))
                    }
                    pokazKalendarz = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pokazKalendarz = false }) { Text("Anuluj") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    // Dialog kategorii
    if (pokazKategorie) {
        var nowaKategoria by remember { mutableStateOf("") }
        var tymczasowe by remember { mutableStateOf(wybrane) }
        AlertDialog(
            onDismissRequest = { pokazKategorie = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Wybierz kategorię") },
            text = {
                Column {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(kategorie) { kat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tymczasowe = if (tymczasowe.contains(kat.nazwa)) {
                                            tymczasowe - kat.nazwa
                                        } else {
                                            tymczasowe + kat.nazwa
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tymczasowe.contains(kat.nazwa),
                                    onCheckedChange = {
                                        tymczasowe = if (it) tymczasowe + kat.nazwa else tymczasowe - kat.nazwa
                                    }
                                )
                                Text(kat.nazwa, color = Color.Black)
                            }
                        }
                    }
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dodaj nową kategorię", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = nowaKategoria,
                            onValueChange = { nowaKategoria = it },
                            label = { Text("Nazwa") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nowaKategoria.isNotEmpty()) {
                                    scope.launch {
                                        userDatabase.categoryDao().insert(Category(nazwa = nowaKategoria))
                                        tymczasowe = tymczasowe + nowaKategoria
                                        nowaKategoria = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF639922))
                        ) { Text("Dodaj") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    wybrane = tymczasowe
                    pokazKategorie = false
                }) {
                    Text("Wybierz", color = Color(0xFF639922))
                }
            },
            dismissButton = {
                TextButton(onClick = { pokazKategorie = false }) {
                    Text("Anuluj", color = Color(0xFF1565C0))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Szczegóły leku",
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
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dane z bazy leków
            daneBazy?.let { baza ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3DE)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informacje z bazy leków", fontSize = 12.sp, color = Color(0xFF639922), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        baza.postac?.let { InfoRow("Postać", it) }
                        baza.moc?.let { InfoRow("Moc", it) }
                        baza.substancjaCzynna?.let { InfoRow("Substancja czynna", it) }
                        baza.nazwaWytworcy?.let { InfoRow("Producent", it) }
                        baza.drogaPodania?.let { InfoRow("Droga podania", it) }
                    }
                }
            }

            // Edycja danych
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Moje dane", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = nazwa,
                        onValueChange = { nazwa = it },
                        label = { Text("Nazwa leku *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = dataWaznosci,
                        onValueChange = {},
                        label = { Text("Data ważności *") },
                        modifier = Modifier.fillMaxWidth().clickable { pokazKalendarz = true },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            IconButton(onClick = { pokazKalendarz = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = ilosc,
                        onValueChange = { ilosc = it },
                        label = { Text("Ilość") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = jednostka,
                        onValueChange = { jednostka = it },
                        label = { Text("Jednostka (np. szt, ml, mg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = wybrane.joinToString(", "),
                        onValueChange = {},
                        label = { Text("Kategoria") },
                        modifier = Modifier.fillMaxWidth().clickable { pokazKategorie = true },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            IconButton(onClick = { pokazKategorie = true }) {
                                Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = uwagi,
                        onValueChange = { uwagi = it },
                        label = { Text("Uwagi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (nazwa.isNotEmpty() && dataWaznosci.isNotEmpty()) {
                        scope.launch {
                            userDatabase.userMedicineDao().update(
                                lek.copy(
                                    nazwa = nazwa,
                                    dataWaznosci = dataWaznosci,
                                    ilosc = ilosc.toIntOrNull(),
                                    jednostka = jednostka.ifEmpty { null },
                                    kategoria = wybrane.joinToString(", ").ifEmpty { null },
                                    uwagi = uwagi.ifEmpty { null }
                                )
                            )
                            onZapisz()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF639922))
            ) {
                Text("Zapisz zmiany", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, color = Color.Black)
    }
}