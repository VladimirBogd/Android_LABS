package com.example.android_labs.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiExpenseResponse(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: ApiExpense?
)
