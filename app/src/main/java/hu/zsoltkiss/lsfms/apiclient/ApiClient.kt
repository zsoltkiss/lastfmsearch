package hu.zsoltkiss.lsfms.apiclient

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiInterface {

    companion object {
        const val BASE_URL = "http://ws.audioscrobbler.com/2.0/"
        const val API_KEY = "f03fc70f49cd4120a587283bd8f11bd3"
    }


//    @GET("/")
//    fun artistSearch(@QueryMap options: Map<String, String>): Call<SearchResults>

    @GET("/")
    fun artistSearch(@QueryMap options: Map<String, String>): Call<ResponseBody>

}

object ApiClient {

    val getClient: ApiInterface
        get() {

            val gson = GsonBuilder()
                .setLenient()
                .create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(ApiInterface::class.java)

        }

}