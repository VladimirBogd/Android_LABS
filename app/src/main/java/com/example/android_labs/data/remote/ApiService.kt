package com.example.android_labs.data.remote

import com.example.android_labs.data.remote.model.ApiExpense
import com.example.android_labs.data.remote.model.ApiExpenseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("expenses/")
    suspend fun getAllExpenses(): Response<List<ApiExpense>>

    @POST("expenses/")
    suspend fun createExpense(@Body expense: ApiExpense): Response<ApiExpenseResponse>

    @GET("expenses/{id}/")
    suspend fun getExpenseById(@Path("id") id: Long): Response<ApiExpense>

    @PUT("expenses/{id}/")
    suspend fun updateExpense(
        @Path("id") id: Long,
        @Body expense: ApiExpense
    ): Response<ApiExpenseResponse>

    @DELETE("expenses/{id}/")
    suspend fun deleteExpense(@Path("id") id: Long): Response<Unit>
}
