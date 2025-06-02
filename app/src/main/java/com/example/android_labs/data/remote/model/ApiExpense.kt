package com.example.android_labs.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiExpense(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double
)
