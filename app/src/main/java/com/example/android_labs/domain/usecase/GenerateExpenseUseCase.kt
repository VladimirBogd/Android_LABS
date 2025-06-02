package com.example.android_labs.domain.usecase

import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.repository.ExpenseRepository
import javax.inject.Inject

class GenerateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(expense: Expense) {
        repository.generateExpense(expense)
    }
}