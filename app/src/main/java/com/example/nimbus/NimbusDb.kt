package com.example.nimbus

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NimbusDb(context: Context) : SQLiteOpenHelper(context, "nimbus.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE world (
                id    INTEGER PRIMARY KEY AUTOINCREMENT,
                name  TEXT NOT NULL,
                about TEXT NOT NULL DEFAULT ''
            )""")
        db.execSQL("""
            CREATE TABLE person (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                world_id    INTEGER NOT NULL REFERENCES world(id) ON DELETE CASCADE,
                name        TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT ''
            )""")
        db.execSQL("""
            CREATE TABLE place (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                world_id    INTEGER NOT NULL REFERENCES world(id) ON DELETE CASCADE,
                name        TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT ''
            )""")
        db.execSQL("""
            CREATE TABLE event (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                world_id    INTEGER NOT NULL REFERENCES world(id) ON DELETE CASCADE,
                title       TEXT NOT NULL,
                year        INTEGER NOT NULL,
                place_id    INTEGER REFERENCES place(id) ON DELETE SET NULL,
                description TEXT NOT NULL DEFAULT ''
            )""")
        db.execSQL("""
            CREATE TABLE event_person (
                event_id  INTEGER NOT NULL REFERENCES event(id) ON DELETE CASCADE,
                person_id INTEGER NOT NULL REFERENCES person(id) ON DELETE CASCADE,
                PRIMARY KEY (event_id, person_id)
            )""")
        db.execSQL("""
            CREATE TABLE dictionary_entry (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                world_id INTEGER NOT NULL REFERENCES world(id) ON DELETE CASCADE,
                word     TEXT NOT NULL,
                meaning  TEXT NOT NULL DEFAULT ''
            )""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        for (table in listOf("event_person", "event", "dictionary_entry", "person", "place", "world")) {
            db.execSQL("DROP TABLE IF EXISTS $table")
        }
        onCreate(db)
    }

    // Runs a SELECT and turns every row into an object
    private fun <T> query(sql: String, args: Array<String>, toItem: (Cursor) -> T): List<T> {
        val result = mutableListOf<T>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) result.add(toItem(cursor))
        }
        return result
    }

    // ---------- worlds ----------
    fun addWorld(name: String, about: String) {
        writableDatabase.insert("world", null, ContentValues().apply {
            put("name", name)
            put("about", about)
        })
    }

    fun getWorlds(): List<World> =
        query("SELECT id, name, about FROM world ORDER BY id", arrayOf()) {
            World(it.getLong(0), it.getString(1), it.getString(2))
        }

    fun getWorld(id: Long): World? =
        query("SELECT id, name, about FROM world WHERE id = ?", arrayOf(id.toString())) {
            World(it.getLong(0), it.getString(1), it.getString(2))
        }.firstOrNull()

    // ---------- characters ----------
    fun addPerson(worldId: Long, name: String, description: String) {
        writableDatabase.insert("person", null, ContentValues().apply {
            put("world_id", worldId)
            put("name", name)
            put("description", description)
        })
    }

    fun getPeople(worldId: Long): List<Person> =
        query("SELECT id, name, description FROM person WHERE world_id = ? ORDER BY name",
            arrayOf(worldId.toString())) {
            Person(it.getLong(0), it.getString(1), it.getString(2))
        }

    fun getPerson(id: Long): Person? =
        query("SELECT id, name, description FROM person WHERE id = ?", arrayOf(id.toString())) {
            Person(it.getLong(0), it.getString(1), it.getString(2))
        }.firstOrNull()

    fun getEventsForPerson(personId: Long): List<String> =
        query("""
            SELECT e.year, e.title FROM event e
            JOIN event_person ep ON ep.event_id = e.id
            WHERE ep.person_id = ?
            ORDER BY e.year""", arrayOf(personId.toString())) {
            "Year ${it.getInt(0)}: ${it.getString(1)}"
        }

    // ---------- places ----------
    fun addPlace(worldId: Long, name: String, description: String) {
        writableDatabase.insert("place", null, ContentValues().apply {
            put("world_id", worldId)
            put("name", name)
            put("description", description)
        })
    }

    fun getPlaces(worldId: Long): List<Place> =
        query("SELECT id, name, description FROM place WHERE world_id = ? ORDER BY name",
            arrayOf(worldId.toString())) {
            Place(it.getLong(0), it.getString(1), it.getString(2))
        }

    fun getPlace(id: Long): Place? =
        query("SELECT id, name, description FROM place WHERE id = ?", arrayOf(id.toString())) {
            Place(it.getLong(0), it.getString(1), it.getString(2))
        }.firstOrNull()

    fun getEventsAtPlace(placeId: Long): List<String> =
        query("SELECT year, title FROM event WHERE place_id = ? ORDER BY year",
            arrayOf(placeId.toString())) {
            "Year ${it.getInt(0)}: ${it.getString(1)}"
        }

    // ---------- timeline ----------
    fun addEvent(worldId: Long, title: String, year: Int, placeId: Long?,
                 description: String, personIds: List<Long>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val eventId = db.insert("event", null, ContentValues().apply {
                put("world_id", worldId)
                put("title", title)
                put("year", year)
                if (placeId != null) put("place_id", placeId) else putNull("place_id")
                put("description", description)
            })
            for (personId in personIds) {
                db.insert("event_person", null, ContentValues().apply {
                    put("event_id", eventId)
                    put("person_id", personId)
                })
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getTimeline(worldId: Long): List<TimelineEntry> =
        query("""
            SELECT e.id, e.title, e.year, e.description, pl.name, GROUP_CONCAT(pe.name, ', ')
            FROM event e
            LEFT JOIN place pl        ON pl.id = e.place_id
            LEFT JOIN event_person ep ON ep.event_id = e.id
            LEFT JOIN person pe       ON pe.id = ep.person_id
            WHERE e.world_id = ?
            GROUP BY e.id
            ORDER BY e.year, e.id""", arrayOf(worldId.toString())) {
            TimelineEntry(
                id = it.getLong(0),
                title = it.getString(1),
                year = it.getInt(2),
                description = it.getString(3),
                placeName = if (it.isNull(4)) null else it.getString(4),
                people = if (it.isNull(5)) null else it.getString(5)
            )
        }

    // ---------- dictionary ----------
    fun addWord(worldId: Long, word: String, meaning: String) {
        writableDatabase.insert("dictionary_entry", null, ContentValues().apply {
            put("world_id", worldId)
            put("word", word)
            put("meaning", meaning)
        })
    }

    fun getWords(worldId: Long): List<DictionaryEntry> =
        query("SELECT id, word, meaning FROM dictionary_entry WHERE world_id = ? ORDER BY word",
            arrayOf(worldId.toString())) {
            DictionaryEntry(it.getLong(0), it.getString(1), it.getString(2))
        }
}