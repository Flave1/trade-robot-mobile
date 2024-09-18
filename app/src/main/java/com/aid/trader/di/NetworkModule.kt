package com.aid.trader.di

import com.aid.trader.data.BrokerageRepositoryImpl
import com.aid.trader.data.ForexForexRepositoryImpl
import com.aid.trader.interfaces.BrokerRepositoryInterface
import com.aid.trader.interfaces.ForexRepositoryInterface
import com.aid.trader.service.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideRetrofit() }
    single { provideApiService(get()) }
    single<ForexRepositoryInterface> { ForexForexRepositoryImpl(get()) }
    single<BrokerRepositoryInterface> { BrokerageRepositoryImpl(get()) }
}

val headerInterceptor = object : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "multipart/form-data")
            .header("Authorization", "Bearer YOUR_TOKEN_HERE") // If authorization is needed
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

fun provideRetrofit(): Retrofit {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .addInterceptor(headerInterceptor)
        .followSslRedirects(true)
        .build()

    return Retrofit.Builder()
        .baseUrl("https://morning-reaches-26919-f556f45469d2.herokuapp.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}
