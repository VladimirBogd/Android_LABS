package com.example.android_labs.data.repository

import com.example.android_labs.data.local.ExpenseDao
import com.example.android_labs.data.local.ExpenseEntity
import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { expenses ->
            expenses.map { it.toDomain() }
        }
    }

    override suspend fun getTotalExpenses(): Double {
        return expenseDao.getTotalExpenses() ?: 0.0
    }

    override fun generateExpense(expense: Expense) {
        expenseDao.generateExpense(expense.toEntity())
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            category = category,
            price = price
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            category = category,
            price = price
        )
    }
}