package com.pixelsnap.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "snaps")
data class Snap(
    @PrimaryKey val id: String,
    val imagePath: String,          // Internal storage path or MediaStore uri string
    val caption: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
