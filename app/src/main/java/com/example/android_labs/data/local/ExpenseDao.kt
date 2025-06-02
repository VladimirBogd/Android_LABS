package com.example.android_labs.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(price) FROM expenses")
    suspend fun getTotalExpenses(): Double?

    @Insert
    fun generateExpense(expense: ExpenseEntity): Long
}