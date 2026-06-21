package com.example.greetingcard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineFormScreen(
    lek: Medicine?,  // nullable!
    onWstecz: () -> Unit,
    onZapisz: () -> Unit,
    userDatabase: UserDatabase
) {
    var dataWaznosci by remember { mutableStateOf("") }
    var ilosc by remember { mutableStateOf("") }
    var jednostka by remember { mutableStateOf("") }
    var wybrane by remember { mutableStateOf<Set<String>>(emptySet()) }
    var uwagi by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var nazwaManualna by remember { mutableStateOf("") }
    val kategorie by userDatabase.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var pokazKategorie by remember { mutableStateOf(false) }

    if (lek == null) {
        OutlinedTextField(
            value = nazwaManualna,
            onValueChange = { nazwaManualna = it },
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
    }

    BackHandler { onWstecz() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        lek?.nazwa ?: "Dodaj lek",
                        fontWeight = FontWeight.Bold,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
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
            // Info o leku z bazy
            lek?.let { l ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informacje z bazy", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(l.nazwa ?: "", fontWeight = FontWeight.Bold)
                        l.postac?.let { Text(it, fontSize = 13.sp, color = Color.Gray) }
                        l.substancjaCzynna?.let { Text(it, fontSize = 13.sp, color = Color.Gray) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            var nazwaManualna by remember { mutableStateOf("") }

            if (lek == null) {
                OutlinedTextField(
                    value = nazwaManualna,
                    onValueChange = { nazwaManualna = it },
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
            }

            // Data ważności (obowiązkowa)
            var pokazKalendarz by remember { mutableStateOf(false) }

            if (pokazKalendarz) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { pokazKalendarz = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                dataWaznosci = formatter.format(java.util.Date(millis))
                            }
                            pokazKalendarz = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pokazKalendarz = false }) { Text("Anuluj") }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }

            OutlinedTextField(
                value = dataWaznosci,
                onValueChange = {},
                label = { Text("Data ważności *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pokazKalendarz = true },
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    IconButton(onClick = { pokazKalendarz = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Wybierz datę")
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

            // Ilość
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

            // Jednostka
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

            // Kategoria
            if (pokazKategorie) {
                var nowaKategoria by remember { mutableStateOf("") }
                var tymczasowe by remember { mutableStateOf(wybrane) }
                val kategorie by userDatabase.categoryDao().getAllCategories().collectAsState(initial = emptyList())

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

            OutlinedTextField(
                value = wybrane.joinToString(", "),
                onValueChange = {},
                label = { Text("Kategoria") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pokazKategorie = true },
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

            // Uwagi
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

            Spacer(modifier = Modifier.height(8.dp))

            // Przycisk zapisz
            Button(
                onClick = {
                    if (dataWaznosci.isNotEmpty()) {
                        scope.launch {
                            userDatabase.userMedicineDao().insert(
                                UserMedicine(
                                    nazwa = lek?.nazwa ?: nazwaManualna,
                                    dataWaznosci = dataWaznosci,
                                    kategoria = wybrane.joinToString(", ").ifEmpty { null },
                                    ilosc = ilosc.toIntOrNull(),
                                    jednostka = jednostka.ifEmpty { null },
                                    uwagi = uwagi.ifEmpty { null },
                                    medicineId = lek?.id,
                                    postac = lek?.postac,
                                    substancjaCzynna = lek?.substancjaCzynna
                                )
                            )
                            onZapisz()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF639922))
            ) {
                Text("Zapisz lek", fontSize = 16.sp)
            }
        }
    }
}