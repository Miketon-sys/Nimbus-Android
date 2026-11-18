package com.example.nimbus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

data class ListLine(val title: String, val subtitle: String)

@Composable
fun ListScreen(
    modifier: Modifier,
    title: String,
    rows: List<ListLine>,
    emptyText: String,
    addLabel: String,
    onAdd: () -> Unit,
    onRowClick: (Int) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (rows.isEmpty()) {
            Text(emptyText, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rows) { index, row ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onRowClick(index) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(row.title, style = MaterialTheme.typography.titleLarge)
                            if (row.subtitle.isNotBlank()) Text(row.subtitle)
                        }
                    }
                }
            }
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text(addLabel) }
    }
}

@Composable
fun FormScreen(
    modifier: Modifier,
    title: String,
    label1: String,
    label2: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = first, onValueChange = { first = it },
            label = { Text(label1) }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = second, onValueChange = { second = it },
            label = { Text(label2) }, modifier = Modifier.fillMaxWidth(), minLines = 3
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (first.isNotBlank()) onSave(first.trim(), second.trim()) }) {
                Text("Save")
            }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
fun WorldHomeScreen(modifier: Modifier, world: World, onOpen: (Screen) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(world.name, style = MaterialTheme.typography.headlineMedium)
        if (world.about.isNotBlank()) Text(world.about)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onOpen(Screen.People(world.id)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Characters")
        }
        Button(onClick = { onOpen(Screen.Places(world.id)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Places")
        }
        Button(onClick = { onOpen(Screen.Timeline(world.id)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Timeline")
        }
        Button(onClick = { onOpen(Screen.Dictionary(world.id)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Dictionary")
        }
    }
}

@Composable
fun DetailScreen(
    modifier: Modifier,
    title: String,
    description: String,
    sectionTitle: String,
    lines: List<String>,
    emptyText: String
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(description)
        }
        Spacer(Modifier.height(24.dp))
        Text(sectionTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (lines.isEmpty()) Text(emptyText)
        LazyColumn {
            items(lines) { line ->
                Text(line, modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }
}

@Composable
fun AddEventScreen(
    modifier: Modifier,
    places: List<Place>,
    people: List<Person>,
    onSave: (title: String, year: Int, placeId: Long?, description: String, personIds: List<Long>) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var placeId by remember { mutableStateOf<Long?>(null) }
    val chosenPeople = remember { mutableStateListOf<Long>() }

    fun togglePerson(id: Long) {
        if (id in chosenPeople) chosenPeople.remove(id) else chosenPeople.add(id)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("New event", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Title") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = yearText, onValueChange = { yearText = it },
            label = { Text("Year (a number)") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("What happened?") }, modifier = Modifier.fillMaxWidth(), minLines = 3
        )

        Spacer(Modifier.height(16.dp))
        Text("Where did it happen?", style = MaterialTheme.typography.titleMedium)
        if (places.isEmpty()) Text("No places yet. Add some under Places first.")
        for (place in places) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { placeId = place.id }
            ) {
                RadioButton(selected = placeId == place.id, onClick = { placeId = place.id })
                Text(place.name)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Who was involved?", style = MaterialTheme.typography.titleMedium)
        if (people.isEmpty()) Text("No characters yet. Add some under Characters first.")
        for (person in people) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { togglePerson(person.id) }
            ) {
                Checkbox(checked = person.id in chosenPeople, onCheckedChange = { togglePerson(person.id) })
                Text(person.name)
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val year = yearText.toIntOrNull()
                if (title.isNotBlank() && year != null) {
                    onSave(title.trim(), year, placeId, description.trim(), chosenPeople.toList())
                }
            }) { Text("Save") }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier, onMyWorlds: () -> Unit, onCreate: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nimbus",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "The world doesn't need more content.\nIt needs more makers.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Create a world")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onMyWorlds, modifier = Modifier.fillMaxWidth()) {
            Text("My worlds")
        }
    }
}