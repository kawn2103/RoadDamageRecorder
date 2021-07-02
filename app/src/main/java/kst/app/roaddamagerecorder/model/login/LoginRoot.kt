package kst.app.roaddamagerecorder.model.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginRoot(
    @SerializedName("code")
    @Expose
    var code: Int? = null,

    @SerializedName("message")
    @Expose
    val message: String? = null,

    @SerializedName("data")
    @Expose
    val data: Data? = null
)