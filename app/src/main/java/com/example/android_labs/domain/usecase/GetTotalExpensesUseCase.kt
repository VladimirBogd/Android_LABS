package com.example.android_labs.domain.usecase

import com.example.android_labs.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetTotalExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): Double {
        return repository.getTotalExpenses() ?: 0.0
    }
}