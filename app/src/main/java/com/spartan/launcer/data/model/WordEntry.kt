package com.spartan.launcer.data.model

data class WordEntry(
    val word: String,
    val meaning: String,
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)