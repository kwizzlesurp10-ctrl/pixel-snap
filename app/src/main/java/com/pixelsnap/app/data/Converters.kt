package com.pixelsnap.app.data

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else json.decodeFromString(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return json.encodeToString(list)
    }
}
