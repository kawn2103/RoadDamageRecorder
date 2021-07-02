package kst.app.roaddamagerecorder.model.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginSend(
    @SerializedName("user_id")
    @Expose
    var id: String? = null,

    @SerializedName("user_pwd")
    @Expose
    val userPw: String? = null,

    @SerializedName("user_hw_name")
    @Expose
    val userHwName: String? = null,

    @SerializedName("user_hw_model")
    @Expose
    val userHwModel: String? = null,

    @SerializedName("user_hw_maker")
    @Expose
    val userHwMaker: String? = null,

    @SerializedName("user_hw_device")
    @Expose
    val userHwDevice: String? = null,

    @SerializedName("user_hw_board")
    @Expose
    val userHwBoard: String? = null,

    @SerializedName("user_hw_hw")
    @Expose
    val userHwHw: String? = null,

    @SerializedName("user_hw_brand")
    @Expose
    val userHwBrand: String? = null,

    @SerializedName("user_hw_id")
    @Expose
    val userHwId: String? = null,

    @SerializedName("user_hw_serial")
    @Expose
    val userHwSerial: String? = null
)