package com.example.todolist.data.local

import android.content.Context
import androidx.room.Room

object DbProvider {
    fun create(context: Context): AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "todo.db"
    ).build()
}