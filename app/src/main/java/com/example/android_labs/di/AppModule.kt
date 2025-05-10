package com.example.android_labs.di

import com.example.android_labs.data.local.ExpenseDao
import com.example.android_labs.data.local.ExpenseDatabase
import com.example.android_labs.data.repository.ExpenseRepositoryImpl
import com.example.android_labs.domain.repository.ExpenseRepository
import com.example.android_labs.domain.usecase.*

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return ExpenseDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: ExpenseDatabase) = database.expenseDao()

    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository {
        return ExpenseRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideGetExpensesUseCase(repository: ExpenseRepository) = GetExpensesUseCase(repository)

    @Provides
    @Singleton
    fun provideAddExpenseUseCase(repository: ExpenseRepository) = AddExpenseUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteExpenseUseCase(repository: ExpenseRepository) = DeleteExpenseUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTotalExpensesUseCase(repository: ExpenseRepository) = GetTotalExpensesUseCase(repository)
}