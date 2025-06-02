package com.example.android_labs.domain.repository

import com.example.android_labs.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getTotalExpenses(): Double?
    fun generateExpense(expense: Expense)
}