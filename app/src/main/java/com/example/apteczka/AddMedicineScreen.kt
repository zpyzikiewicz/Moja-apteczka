package com.example.apteczka

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    onWstecz: () -> Unit,
    database: MedicineDatabase,
    onLekWybrany: (Medicine) -> Unit,
    onDodajWlasny: () -> Unit
) {
    var szukaj by remember { mutableStateOf("") }
    var wyniki by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    val scope = rememberCoroutineScope()

    BackHandler { onWstecz() }

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
        ) {
            // Pole wyszukiwania
            OutlinedTextField(
                value = szukaj,
                onValueChange = { nowy ->
                    szukaj = nowy
                    if (nowy.length >= 2) {
                        scope.launch {
                            wyniki = database.medicineDao().szukajLeku(nowy)
                        }
                    } else {
                        wyniki = emptyList()
                    }
                },
                label = { Text("Wyszukaj lek z bazy...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Wyniki wyszukiwania
            if (wyniki.isEmpty() && szukaj.length >= 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nie znaleziono leku", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wyniki) { lek ->
                        WynikLekuCard(lek = lek, onClick = {
                            onLekWybrany(lek)
                        })
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Lub dodaj własny lek",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDodajWlasny() },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF639922)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✏️", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dodaj własny lek", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    Text("Wpisz dane ręcznie", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("›", fontSize = 22.sp, color = Color(0xFF639922))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WynikLekuCard(lek: Medicine, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(lek.nazwa ?: "Brak nazwy", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            lek.postac?.let { postac ->
                if (postac.isNotEmpty()) {
                    Text(postac, fontSize = 13.sp, color = Color.Gray)
                }
            }
            lek.substancjaCzynna?.let { substancja ->
                Text(substancja, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}