package com.example.android_labs.domain.model

data class Expense(
    val id: Long = 0,
    val category: String,
    val price: Double
)