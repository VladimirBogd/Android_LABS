package com.example.android_labs.domain.usecase

import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.deleteExpense(expense)
    }
}