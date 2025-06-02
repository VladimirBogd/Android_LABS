package com.example.android_labs.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    val category = MutableLiveData<String>()
    val price = MutableLiveData<String>()

    fun addExpense() {
        val categoryValue = category.value.orEmpty()
        val priceText = price.value.orEmpty()

        viewModelScope.launch {
            try {
                if (categoryValue.isBlank()) {
                    Log.e("AddExpense", "Поле Категория пустое")
                    return@launch
                }

                if (priceText.isBlank()) {
                    Log.e("AddExpense", "Поле Цена пустое")
                    return@launch
                }

                val priceValue = priceText.toDoubleOrNull()
                if (priceValue == null || priceValue <= 0) {
                    Log.e("AddExpense", "Поле цена должно быть положительным: $priceText")
                    return@launch
                }

                addExpenseUseCase(Expense(category = categoryValue, price = priceValue))
                Log.i("AddExpense", "Покупка добавлена успешно")

            } catch (e: Exception) {
                Log.e("AddExpense", "Ошибка добавления покупки: ${e.message}")
            }
        }
    }
}
