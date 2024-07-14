package com.andhika185.lexica.db.converters

import androidx.room.TypeConverter
import com.andhika185.lexica.db.GameMode

class GameModeTypeConverter {

    @TypeConverter
    fun fromType(value: GameMode.Type): String {
        return value.name
    }

    @TypeConverter
    fun toType(value: String): GameMode.Type {
        return GameMode.Type.valueOf(value)
    }

}