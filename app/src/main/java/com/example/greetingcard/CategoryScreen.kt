package com.example.greetingcard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onWstecz: () -> Unit,
    userDatabase: UserDatabase,
    onKategoriaWybrana: (String) -> Unit
) {
    val kategorie by userDatabase.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var nowaKategoria by remember { mutableStateOf("") }
    var edytowanaKategoria by remember { mutableStateOf<Category?>(null) }

    BackHandler { onWstecz() }

    // Dialog edycji
    edytowanaKategoria?.let { kat ->
        var edytowanyTekst by remember { mutableStateOf(kat.nazwa) }
        AlertDialog(
            onDismissRequest = { edytowanaKategoria = null },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Edytuj kategorię") },
            text = {
                OutlinedTextField(
                    value = edytowanyTekst,
                    onValueChange = { edytowanyTekst = it },
                    label = { Text("Nazwa kategorii") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        userDatabase.categoryDao().update(kat.copy(nazwa = edytowanyTekst))
                        userDatabase.userMedicineDao().updateKategoria(kat.nazwa, edytowanyTekst)
                    }
                    edytowanaKategoria = null
                }) { Text("Zapisz", color = Color(0xFF1565C0)) }
            },
            dismissButton = {
                TextButton(onClick = { edytowanaKategoria = null }) { Text("Anuluj", color = Color(0xFF1565C0)) }
            }
        )
    }

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
            // Dodaj nową kategorię
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nowaKategoria,
                    onValueChange = { nowaKategoria = it },
                    label = { Text("Nowa kategoria") },
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
                                nowaKategoria = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF639922))
                ) {
                    Text("Dodaj")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (kategorie.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak kategorii", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(kategorie, key = { it.id }) { kat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onKategoriaWybrana(kat.nazwa) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    kat.nazwa,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { edytowanaKategoria = kat }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = Color(0xFF639922))
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        userDatabase.categoryDao().delete(kat)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}