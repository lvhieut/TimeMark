package com.example.timemarkbase.time_mark

enum class TimeMarkStyle(
    val displayName: String
) {
    CLASSIC("Kiểu 1"),
    COMPACT("Kiểu 2");

    companion object {
        fun fromName(value: String?): TimeMarkStyle {
            return entries.firstOrNull { it.name == value } ?: CLASSIC
        }
    }
}
