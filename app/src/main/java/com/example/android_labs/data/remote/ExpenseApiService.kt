//package com.example.android_labs.data.remote
//
//interface ExpenseApiService {
//    @GET("categories")
//    suspend fun getCategories(): List<String>
//
//    @GET("expenses")
//    suspend fun getExpensesByCategory(@Query("category") category: String): List<ExpenseDto>
//
//    @POST("expenses")
//    suspend fun addExpense(@Body expense: ExpenseDto): Response<Unit>
//}
