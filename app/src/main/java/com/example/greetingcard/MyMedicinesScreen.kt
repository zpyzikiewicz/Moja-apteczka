package com.example.greetingcard


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMedicinesScreen(
    onWstecz: () -> Unit,
    userDatabase: UserDatabase,
    tytul: String = "Moje leki",
    filtrData: String? = null,
    kategoriaFiltr: String? = null,
    filtrPrzeterminowane: Boolean = false,
    onLekWybrany: (UserMedicine) -> Unit = {}
) {
    var szukaj by remember { mutableStateOf("") }
    var wybranaKategoria by remember { mutableStateOf<String?>(null) }
    var dataOd by remember { mutableStateOf<String?>(null) }
    var dataDo by remember { mutableStateOf<String?>(null) }
    var pokazFiltry by remember { mutableStateOf(false) }
    var pokazKalendarzOd by remember { mutableStateOf(false) }
    var pokazKalendarzDo by remember { mutableStateOf(false) }
    val kategorie by userDatabase.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    val formatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }

    val wszystkieLeki by userDatabase.userMedicineDao().getAllMedicines().collectAsState(initial = emptyList())
    val dzisiajLokalnie = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    val wygasajaceLeki by if (filtrData != null) {
        userDatabase.userMedicineDao().getWygasajaceLeki(dzisiajLokalnie, filtrData)
    } else {
        userDatabase.userMedicineDao().getAllMedicines()
    }.collectAsState(initial = emptyList())

    val dzisiaj = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    val przeterminowaneLeki by if (filtrPrzeterminowane) {
        userDatabase.userMedicineDao().getPrzeterminowaneLeki(dzisiaj)
    } else {
        userDatabase.userMedicineDao().getAllMedicines()
    }.collectAsState(initial = emptyList())

    val leki = when {
        szukaj.isNotEmpty() && filtrPrzeterminowane -> przeterminowaneLeki.filter { it.nazwa.contains(szukaj, ignoreCase = true) }
        filtrPrzeterminowane -> przeterminowaneLeki
        szukaj.isNotEmpty() && filtrData != null -> wygasajaceLeki.filter { it.nazwa.contains(szukaj, ignoreCase = true) }
        szukaj.isNotEmpty() && kategoriaFiltr != null -> userDatabase.userMedicineDao().getLekiByKategoria(kategoriaFiltr).collectAsState(initial = emptyList()).value.filter { it.nazwa.contains(szukaj, ignoreCase = true) }
        szukaj.isNotEmpty() -> wszystkieLeki.filter { it.nazwa.contains(szukaj, ignoreCase = true) }
        kategoriaFiltr != null -> userDatabase.userMedicineDao().getLekiByKategoria(kategoriaFiltr).collectAsState(initial = emptyList()).value
        wybranaKategoria != null && dataOd != null && dataDo != null ->
            userDatabase.userMedicineDao().getLekiByKategoriaIData(wybranaKategoria!!, dataOd!!, dataDo!!).collectAsState(initial = emptyList()).value
        wybranaKategoria != null ->
            userDatabase.userMedicineDao().getLekiByKategoria(wybranaKategoria!!).collectAsState(initial = emptyList()).value
        dataOd != null && dataDo != null ->
            userDatabase.userMedicineDao().getLekiByData(dataOd!!, dataDo!!).collectAsState(initial = emptyList()).value
        filtrData != null -> wygasajaceLeki
        else -> wszystkieLeki
    }

    var zaladowano by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        zaladowano = true
    }

    BackHandler { onWstecz() }


    // Kalendarz Od
    if (pokazKalendarzOd) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pokazKalendarzOd = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataOd = formatter.format(java.util.Date(millis))
                    }
                    pokazKalendarzOd = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pokazKalendarzOd = false }) { Text("Anuluj") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

// Kalendarz Do
    if (pokazKalendarzDo) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pokazKalendarzDo = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataDo = formatter.format(java.util.Date(millis))
                    }
                    pokazKalendarzDo = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pokazKalendarzDo = false }) { Text("Anuluj") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

// Dialog filtrów
    if (pokazFiltry) {
        AlertDialog(
            onDismissRequest = { pokazFiltry = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Filtry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Kategoria
                    Text("Kategoria", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        item {
                            TextButton(
                                onClick = { wybranaKategoria = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Wszystkie",
                                    color = if (wybranaKategoria == null) Color(0xFF639922) else Color.Black
                                )
                            }
                        }
                        items(kategorie) { kat ->
                            TextButton(
                                onClick = { wybranaKategoria = kat.nazwa },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    kat.nazwa,
                                    color = if (wybranaKategoria == kat.nazwa) Color(0xFF639922) else Color.Black
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Data od
                    Text("Data ważności od", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = dataOd ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pokazKalendarzOd = true },
                        label = { Text("Od") },
                        trailingIcon = {
                            IconButton(onClick = { pokazKalendarzOd = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )

                    // Data do
                    Text("Data ważności do", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = dataDo ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pokazKalendarzDo = true },
                        label = { Text("Do") },
                        trailingIcon = {
                            IconButton(onClick = { pokazKalendarzDo = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )

                    // Reset
                    TextButton(
                        onClick = {
                            wybranaKategoria = null
                            dataOd = null
                            dataDo = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Wyczyść filtry", color = Color.Red) }
                }
            },
            confirmButton = {},
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { pokazFiltry = false }) {
                        Text("Anuluj", color = Color(0xFF1565C0))
                    }
                    TextButton(onClick = { pokazFiltry = false }) {
                        Text("Szukaj", color = Color(0xFF639922))
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        tytul,
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
                actions = {
                    if (filtrData == null && kategoriaFiltr == null && !filtrPrzeterminowane) {
                        IconButton(onClick = { pokazFiltry = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtry")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        if (!zaladowano) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else if (leki.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💊", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Brak leków w apteczce", color = Color.Gray)
                    Text("Dodaj pierwszy lek!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = szukaj,
                        onValueChange = { szukaj = it },
                        label = { Text("Szukaj leku...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(leki, key = { it.id }) { lek ->
                    MyMedicineCard(lek = lek, userDatabase = userDatabase, onClick = { onLekWybrany(lek) })
                }
            }
        }
    }
}

@Composable
fun MyMedicineCard(lek: UserMedicine, userDatabase: UserDatabase, onClick: () -> Unit) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lek.nazwa, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "Ważny do: ${lek.dataWaznosci}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                lek.kategoria?.let {
                    Text("Kategoria: $it", fontSize = 13.sp, color = Color.Gray)
                }
                lek.ilosc?.let {
                    Text("Ilość: $it ${lek.jednostka ?: ""}", fontSize = 13.sp, color = Color.Gray)
                }
                lek.uwagi?.let {
                    Text("Uwagi: $it", fontSize = 13.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = {
                scope.launch {
                    userDatabase.userMedicineDao().delete(lek)
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
            }
        }
    }
}