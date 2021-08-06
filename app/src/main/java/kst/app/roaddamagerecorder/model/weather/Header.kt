package kst.app.roaddamagerecorder.model.weather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Header(
    val resultCode : Int,
    val resultMsg : String
)