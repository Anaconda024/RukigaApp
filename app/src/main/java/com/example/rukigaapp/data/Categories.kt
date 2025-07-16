package com.example.rukigaapp.data

enum class Categories(val id: Int, val displayName: String, val color: String) {
    Phrase(1, "Phrase", "#346940"),
    Greeting(2, "Greeting", "#73bfbe"),
    Number(3, "Number", "#a77cc4"),
    Time(4, "Time", "#c47c9a"),
    Other(5, "Other", "#c4a57c");

    companion object {
        fun fromId(id: Int): Categories? = entries.find { it.id == id }
        fun fromDisplayName(name: String): Categories? = entries.find { it.displayName == name }
    }
}