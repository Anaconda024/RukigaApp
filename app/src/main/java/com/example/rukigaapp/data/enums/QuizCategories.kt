package com.example.rukigaapp.data.enums

enum class QuizCategories (val id: Int, val displayName: String) {
    RukigaToEnglish(1, "Rukiga To English"),
    EnglishToRukiga(2, "English To Rukiga"),
    Mixed(3, "Mixed");

    companion object {
        fun fromId(id: Int): Categories? = Categories.entries.find { it.id == id }
        fun fromDisplayName(name: String): Categories? = Categories.entries.find { it.displayName == name }
    }
}