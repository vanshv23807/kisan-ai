package com.example.data.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val letterIcon: String
) {
    ENGLISH("en", "English", "English", "A"),
    HINDI("hi", "Hindi", "हिंदी", "अ"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", "ਪੰ"),
    MARATHI("mr", "Marathi", "मराठी", "म"),
    BENGALI("bn", "Bengali", "বাংলা", "বাং");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
