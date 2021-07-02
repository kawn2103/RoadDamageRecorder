package kst.app.roaddamagerecorder.model.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("user_id") 
    @Expose
    var userId: String? = null,
    
    @SerializedName("user_use_id")
    @Expose
    val userUseId: String? = null,

    @SerializedName("user_pw")
    @Expose
    val userPw: Any? = null,

    @SerializedName("user_name")
    @Expose
    val userName: String? = null,

    @SerializedName("user_sex")
    @Expose
    val userSex: Any? = null,

    @SerializedName("user_age")
    @Expose
    val userAge: Any? = null,

    @SerializedName("user_phone")
    @Expose
    val userPhone: Any? = null,

    @SerializedName("user_address")
    @Expose
    val userAddress: Any? = null,

    @SerializedName("user_address_detail")
    @Expose
    val userAddressDetail: Any? = null,

    @SerializedName("user_join")
    @Expose
    val userJoin: Any? = null,

    @SerializedName("user_equipment")
    @Expose
    val userEquipment: Any? = null,

    @SerializedName("user_bank")
    @Expose
    val userBank: Any? = null,

    @SerializedName("user_birthday")
    @Expose
    val userBirthday: Any? = null,

    @SerializedName("user_holder")
    @Expose
    val userHolder: Any? = null,

    @SerializedName("user_account")
    @Expose
    val userAccount: Any? = null,

    @SerializedName("user_workerid")
    @Expose
    val userWorkerid: Any? = null,

    @SerializedName("user_service_agree")
    @Expose
    val userServiceAgree: Any? = null,

    @SerializedName("user_personal_agree")
    @Expose
    val userPersonalAgree: Any? = null,

    @SerializedName("user_age_agree")
    @Expose
    val userAgeAgree: Any? = null,

    @SerializedName("user_marketing_agree")
    @Expose
    val userMarketingAgree: Any? = null,

    @SerializedName("user_hw_name")
    @Expose
    val userHwName: Any? = null,

    @SerializedName("user_hw_model")
    @Expose
    val userHwModel: Any? = null,

    @SerializedName("user_hw_maker")
    @Expose
    val userHwMaker: Any? = null,

    @SerializedName("user_hw_device")
    @Expose
    val userHwDevice: Any? = null,

    @SerializedName("user_hw_board")
    @Expose
    val userHwBoard: Any? = null,

    @SerializedName("user_hw_hw")
    @Expose
    val userHwHw: Any? = null,

    @SerializedName("user_hw_brand")
    @Expose
    val userHwBrand: Any? = null,

    @SerializedName("user_hw_type")
    @Expose
    val userHwType: Any? = null,

    @SerializedName("user_hw_id")
    @Expose
    val userHwId: Any? = null,

    @SerializedName("user_hw_serial")
    @Expose
    val userHwSerial: Any? = null,

    @SerializedName("createtime")
    @Expose
    val createtime: String? = null,

    @SerializedName("updatetime")
    @Expose
    val updatetime: String? = null,
)