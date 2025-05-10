//package com.example.android_labs.data.remote
//
//import android.net.wifi.WifiConfiguration
//import androidx.constraintlayout.solver.Cache
//import java.io.File
//import java.util.concurrent.TimeUnit
//
//object RetrofitClient {
//    private const val BASE_URL = "https://yourapi.com/"
//    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10 MB
//
//    private val okHttpClient = OkHttpClient.Builder()
//        .cache(Cache(File("cache_dir"), CACHE_SIZE))
//        .protocols(listOf(WifiConfiguration.Protocol.HTTP_2, WifiConfiguration.Protocol.HTTP_1_1))
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .addInterceptor { chain ->
//            val request = chain.request()
//            var response = chain.proceed(request)
//            var tryCount = 0
//
//            while (!response.isSuccessful && tryCount < 3 && request.method == "GET") {
//                tryCount++
//                response = chain.proceed(request)
//            }
//            response
//        }
//        .build()
//
//    val expenseApiService: ExpenseApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ExpenseApiService::class.java)
//    }
//}