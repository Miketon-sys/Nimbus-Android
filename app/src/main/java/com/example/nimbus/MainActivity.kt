package com.example.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.nimbus.ui.theme.CloudWhite
import com.example.nimbus.ui.theme.NimbusTheme

sealed class Screen {
    object Home : Screen()
    object MyWorlds : Screen()
    object CreateWorld : Screen()
    data class WorldHome(val worldId: Long) : Screen()
    data class People(val worldId: Long) : Screen()
    data class AddPerson(val worldId: Long) : Screen()
    data class PersonDetail(val personId: Long) : Screen()
    data class Places(val worldId: Long) : Screen()
    data class AddPlace(val worldId: Long) : Screen()
    data class PlaceDetail(val placeId: Long) : Screen()
    data class Timeline(val worldId: Long) : Screen()
    data class AddEvent(val worldId: Long) : Screen()
    data class Dictionary(val worldId: Long) : Screen()
    data class AddWord(val worldId: Long) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = NimbusDb(applicationContext)
        setContent {
            NimbusTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.nimbus_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentColor = CloudWhite
                    ) { innerPadding ->
                        NimbusApp(db = db, modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
@Composable
fun NimbusApp(db: NimbusDb, modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.MyWorlds) }
    fun go(screen: Screen) { backStack.add(screen) }
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    BackHandler(enabled = backStack.size > 1) { back() }

    when (val screen = backStack.last()) {
        Screen.Home -> HomeScreen(
            modifier = modifier,
            onMyWorlds = { go(Screen.MyWorlds) },
            onCreate = { go(Screen.CreateWorld) }
        )

        Screen.MyWorlds -> {
            val worlds = remember { db.getWorlds() }
            ListScreen(
                modifier = modifier,
                title = "My Worlds",
                rows = worlds.map { ListLine(it.name, it.about) },
                emptyText = "No worlds yet. Create your first one!",
                addLabel = "+ Create a world",
                onAdd = { go(Screen.CreateWorld) },
                onRowClick = { i -> go(Screen.WorldHome(worlds[i].id)) }
            )
        }

        Screen.CreateWorld -> FormScreen(
            modifier = modifier, title = "Create a world",
            label1 = "Name", label2 = "About",
            onSave = { name, about -> db.addWorld(name, about); back() },
            onCancel = { back() }
        )

        is Screen.WorldHome -> {
            val world = remember { db.getWorld(screen.worldId) }
            if (world != null) WorldHomeScreen(modifier = modifier, world = world, onOpen = { go(it) })
        }

        // ----- characters -----
        is Screen.People -> {
            val people = remember { db.getPeople(screen.worldId) }
            ListScreen(
                modifier = modifier,
                title = "Characters",
                rows = people.map { ListLine(it.name, it.description) },
                emptyText = "No characters yet.",
                addLabel = "+ Add a character",
                onAdd = { go(Screen.AddPerson(screen.worldId)) },
                onRowClick = { i -> go(Screen.PersonDetail(people[i].id)) }
            )
        }

        is Screen.AddPerson -> FormScreen(
            modifier = modifier, title = "New character",
            label1 = "Name", label2 = "Description",
            onSave = { name, desc -> db.addPerson(screen.worldId, name, desc); back() },
            onCancel = { back() }
        )

        is Screen.PersonDetail -> {
            val person = remember { db.getPerson(screen.personId) }
            val events = remember { db.getEventsForPerson(screen.personId) }
            if (person != null) DetailScreen(
                modifier = modifier, title = person.name, description = person.description,
                sectionTitle = "Appears in", lines = events,
                emptyText = "Not part of any event yet."
            )
        }

        // ----- places -----
        is Screen.Places -> {
            val places = remember { db.getPlaces(screen.worldId) }
            ListScreen(
                modifier = modifier,
                title = "Places",
                rows = places.map { ListLine(it.name, it.description) },
                emptyText = "No places yet.",
                addLabel = "+ Add a place",
                onAdd = { go(Screen.AddPlace(screen.worldId)) },
                onRowClick = { i -> go(Screen.PlaceDetail(places[i].id)) }
            )
        }

        is Screen.AddPlace -> FormScreen(
            modifier = modifier, title = "New place",
            label1 = "Name", label2 = "Description",
            onSave = { name, desc -> db.addPlace(screen.worldId, name, desc); back() },
            onCancel = { back() }
        )

        is Screen.PlaceDetail -> {
            val place = remember { db.getPlace(screen.placeId) }
            val events = remember { db.getEventsAtPlace(screen.placeId) }
            if (place != null) DetailScreen(
                modifier = modifier, title = place.name, description = place.description,
                sectionTitle = "History", lines = events,
                emptyText = "Nothing has happened here yet."
            )
        }

        // ----- timeline -----
        is Screen.Timeline -> {
            val entries = remember { db.getTimeline(screen.worldId) }
            ListScreen(
                modifier = modifier,
                title = "Timeline",
                rows = entries.map { ListLine("Year ${it.year}: ${it.title}", timelineSubtitle(it)) },
                emptyText = "No events yet.",
                addLabel = "+ Add an event",
                onAdd = { go(Screen.AddEvent(screen.worldId)) }
            )
        }

        is Screen.AddEvent -> {
            val places = remember { db.getPlaces(screen.worldId) }
            val people = remember { db.getPeople(screen.worldId) }
            AddEventScreen(
                modifier = modifier, places = places, people = people,
                onSave = { title, year, placeId, description, personIds ->
                    db.addEvent(screen.worldId, title, year, placeId, description, personIds)
                    back()
                },
                onCancel = { back() }
            )
        }

        // ----- dictionary -----
        is Screen.Dictionary -> {
            val words = remember { db.getWords(screen.worldId) }
            ListScreen(
                modifier = modifier,
                title = "Dictionary",
                rows = words.map { ListLine(it.word, it.meaning) },
                emptyText = "No words yet.",
                addLabel = "+ Add a word",
                onAdd = { go(Screen.AddWord(screen.worldId)) }
            )
        }

        is Screen.AddWord -> FormScreen(
            modifier = modifier, title = "New word",
            label1 = "Word", label2 = "Meaning",
            onSave = { word, meaning -> db.addWord(screen.worldId, word, meaning); back() },
            onCancel = { back() }
        )
    }
}

fun timelineSubtitle(e: TimelineEntry): String {
    var text = ""
    if (e.placeName != null) text += "At ${e.placeName}. "
    if (e.people != null) text += "With ${e.people}. "
    text += e.description
    return text
}