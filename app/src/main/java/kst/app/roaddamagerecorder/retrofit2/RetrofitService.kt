package kst.app.roaddamagerecorder.retrofit2

import kst.app.roaddamagerecorder.model.login.LoginRoot
import kst.app.roaddamagerecorder.model.login.LoginSend
import kst.app.roaddamagerecorder.model.weather.Weather
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {

    @POST("api/user/login")
    fun login(@Body loginSend: LoginSend) : Call<LoginRoot>

    @GET("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?" +
            "serviceKey=j5Fb7Fx2JZyl5Hcevd4NGMtGrQPu5EoXIJoQWJ92SLLAfCU6ZIZSNPKj4mOrf49NGXnZ6VizWJ7MAdOVMdYEiw%3D%3D")
    fun getWeather(
        @Query("dataType") data_type : String,
        @Query("numOfRows") num_of_rows : Int,
        @Query("pageNo") page_no : Int,
        @Query("base_date") base_date : String,
        @Query("base_time") base_time : String,
        @Query("nx") nx : String,
        @Query("ny") ny : String
    ): Call<Weather>
}