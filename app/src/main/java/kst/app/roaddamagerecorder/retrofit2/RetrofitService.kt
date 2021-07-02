package kst.app.roaddamagerecorder.retrofit2

import kst.app.roaddamagerecorder.model.login.LoginRoot
import kst.app.roaddamagerecorder.model.login.LoginSend
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {

    @POST("api/user/login")
    fun login(@Body loginSend: LoginSend) : Call<LoginRoot>
}