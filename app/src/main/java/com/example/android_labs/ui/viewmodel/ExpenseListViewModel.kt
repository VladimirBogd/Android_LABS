package com.example.android_labs.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.usecase.DeleteExpenseUseCase
import com.example.android_labs.domain.usecase.GetExpensesUseCase
import com.example.android_labs.domain.usecase.GetTotalExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase
) : ViewModel() {

    private val _expenses = MutableLiveData<List<Expense>>(emptyList())
    val expenses: LiveData<List<Expense>> = _expenses

    private val _totalExpense = MutableLiveData<Double>(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            getExpensesUseCase().collectLatest { expenses ->
                _expenses.postValue(expenses)
                val total = getTotalExpensesUseCase()
                _totalExpense.postValue(total)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
        }
    }
}
