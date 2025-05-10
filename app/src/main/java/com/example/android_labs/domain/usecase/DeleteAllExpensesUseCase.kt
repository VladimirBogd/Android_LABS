package com.example.android_labs.domain.usecase

import com.example.android_labs.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteAllExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke() {
        val expenses = expenseRepository.getAllExpenses().first()
        expenses.forEach { expenseRepository.deleteExpense(it) }
    }
}