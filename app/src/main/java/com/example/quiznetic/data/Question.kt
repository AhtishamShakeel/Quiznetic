package com.example.quiznetic.data

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.Serializable

data class Question(
    val text: String,
    @JsonAdapter(OptionsAdapter::class)
    val options: List<String>,
    val correctAnswer: Int
) : Serializable

/**
 * Custom adapter to handle options field that can be either a comma-separated string or an array of strings
 */
class OptionsAdapter : TypeAdapter<List<String>>() {
    override fun write(out: JsonWriter?, value: List<String>?) {
        if (value == null) {
            out?.nullValue()
            return
        }
        out?.beginArray()
        for (option in value) {
            out?.value(option)
        }
        out?.endArray()
    }

    override fun read(reader: JsonReader?): List<String> {
        if (reader == null) return emptyList()
        
        return when (reader.peek()) {
            JsonToken.STRING -> {
                // If it's a string, split by comma
                val optionsString = reader.nextString()
                optionsString.split(",").map { it.trim() }
            }
            JsonToken.BEGIN_ARRAY -> {
                // If it's an array, read each element
                val options = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    options.add(reader.nextString())
                }
                reader.endArray()
                options
            }
            else -> {
                // Skip unknown token
                reader.skipValue()
                emptyList()
            }
        }
    }
}