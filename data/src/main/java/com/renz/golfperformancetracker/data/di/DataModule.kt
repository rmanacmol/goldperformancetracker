package com.renz.golfperformancetracker.data.di

import androidx.room.Room
import com.renz.golfperformancetracker.data.NetworkConfig
import com.renz.golfperformancetracker.data.local.GolfDatabase
import com.renz.golfperformancetracker.data.remote.GolfApiService
import com.renz.golfperformancetracker.data.remote.MockGolfInterceptor
import com.renz.golfperformancetracker.data.repository.GolfRepositoryImpl
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import com.renz.golfperformancetracker.util.NetworkMonitor
import com.renz.golfperformancetracker.util.NetworkMonitorImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val dataModule = module {

    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        val config = get<NetworkConfig>()
        val logging = HttpLoggingInterceptor().apply {
            level = if (config.isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(MockGolfInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val config = get<NetworkConfig>()
        Retrofit.Builder()
            .baseUrl(config.apiBaseUrl)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<GolfApiService> { get<Retrofit>().create(GolfApiService::class.java) }

    single {
        Room.databaseBuilder(
            androidContext(),
            GolfDatabase::class.java,
            "golf_performance.db",
        ).fallbackToDestructiveMigration()
            .build()
    }

    single { get<GolfDatabase>().playerDao() }
    single { get<GolfDatabase>().shotDao() }
    single { get<GolfDatabase>().playerDetailDao() }
    single { get<GolfDatabase>().syncMetadataDao() }

    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }

    single<GolfRepository> {
        GolfRepositoryImpl(
            api = get(),
            playerDao = get(),
            shotDao = get(),
            playerDetailDao = get(),
            syncMetadataDao = get(),
            networkMonitor = get(),
            applicationScope = get(),
        )
    }
}
