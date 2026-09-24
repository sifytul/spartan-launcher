package com.spartan.launcer.ui.word

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.model.WordEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordDetailViewModel(
    private val app: SpartanLauncherApp,
    private val word: String
) : ViewModel() {

    private val container = app.container

    private val _entry = MutableStateFlow<WordEntry?>(null)
    val entry: StateFlow<WordEntry?> = _entry.asStateFlow()

    init {
        viewModelScope.launch {
            _entry.value = resolve()
        }
    }

    private suspend fun resolve(): WordEntry? {
        val stored = container.wordOfTheDayRepository.currentStoredEntry()
        if (stored != null && stored.word.equals(word, ignoreCase = true)) return stored
        return container.wordOfTheDayRepository.entryByWord(word)
    }

    companion object {
        fun factory(word: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                WordDetailViewModel(app, word)
            }
        }
    }
}