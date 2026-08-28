package com.example.timemarkbase.time_mark

import com.example.timemarkbase.model.TimeMarkContent

interface TimeMarkRenderer {
    fun bind(content: TimeMarkContent)
    fun applyCommonStyle()
}
