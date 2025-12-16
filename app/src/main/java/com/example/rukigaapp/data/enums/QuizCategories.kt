package com.example.rukigaapp.data.enums

enum class QuizCategories (val id: Int, val displayName: String, val color: String?) {
    RukigaToEnglish(1, "Rukiga To English" ,"#346940" ),
    EnglishToRukiga(2, "English To Rukiga", "#a77cc4"),
    Mixed(3, "Mixed", "#c4a57c");

    companion object {
        fun fromId(id: Int): QuizCategories? = QuizCategories.entries.find { it.id == id }
        fun fromDisplayName(name: String): QuizCategories? = QuizCategories.entries.find { it.displayName == name }
    }
}