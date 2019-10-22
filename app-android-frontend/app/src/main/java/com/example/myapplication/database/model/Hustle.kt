package com.example.myapplication.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HustleStatus {
    completed, in_prog, cancelled, posted
}

@Entity(tableName = "hustle_table")
data class Hustle(
    @PrimaryKey
    var hustleId: String = "",

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "provider")
    val providerId: String,

    @ColumnInfo(name = "date_posted")
    val datePosted: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "price")
    val price: Int,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "categories")
    val categories: List<String>,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "status")
    val status: String
)