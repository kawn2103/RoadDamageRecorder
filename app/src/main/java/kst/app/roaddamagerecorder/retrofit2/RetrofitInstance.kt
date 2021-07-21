package kst.app.roaddamagerecorder.retrofit2

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kst.app.roaddamagerecorder.utils.Utils.baseUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object RetrofitInstanceObject {

        var client = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor()!!).build()

        private val gson : Gson = GsonBuilder().setLenient().create()

        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        private var service: RetrofitService = retrofit.create(RetrofitService::class.java)

        fun getRetrofitService(): RetrofitService {
            return service
        }

        private fun httpLoggingInterceptor(): HttpLoggingInterceptor? {
            val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.i("gwan2103_httpLog", "httpLog ====> $message")
                }
            })
            return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }

    }
}