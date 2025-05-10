package com.example.android_labs.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.usecase.AddExpenseUseCase
import com.example.android_labs.domain.usecase.DeleteAllExpensesUseCase
import com.example.android_labs.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteAllExpensesUseCase: DeleteAllExpensesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorageUiState>(StorageUiState.Idle)
    val uiState: StateFlow<StorageUiState> = _uiState

    private val _currentExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val currentExpenses: StateFlow<List<Expense>> = _currentExpenses

    init {
        loadCurrentExpenses()
    }

    private fun loadCurrentExpenses() {
        viewModelScope.launch {
            getExpensesUseCase().collect { expenses ->
                _currentExpenses.value = expenses
            }
        }
    }

    fun importExpenses(expenses: List<Expense>) {
        viewModelScope.launch {
            _uiState.value = StorageUiState.Loading
            try {
                deleteAllExpensesUseCase()
                expenses.forEach { addExpenseUseCase(it) }
                _uiState.value = StorageUiState.Success("Данные успешно импортированы (${expenses.size} записей)")
            } catch (e: Exception) {
                _uiState.value = StorageUiState.Error("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    sealed class StorageUiState {
        data object Idle : StorageUiState()
        data object Loading : StorageUiState()
        data class Success(val message: String) : StorageUiState()
        data class Error(val message: String) : StorageUiState()
    }
}