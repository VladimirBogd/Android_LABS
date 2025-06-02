package com.example.android_labs.data.remote

import com.example.android_labs.data.remote.model.ApiExpense
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllExpenses() = apiService.getAllExpenses()
    suspend fun createExpense(expense: ApiExpense) = apiService.createExpense(expense)
    suspend fun getExpenseById(id: Long) = apiService.getExpenseById(id)
    suspend fun updateExpense(id: Long, expense: ApiExpense) = apiService.updateExpense(id, expense)
    suspend fun deleteExpense(id: Long) = apiService.deleteExpense(id)
}
