package com.example.android_labs.domain.usecase

import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> {
        return repository.getAllExpenses()
    }
}