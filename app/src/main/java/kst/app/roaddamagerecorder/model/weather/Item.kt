package kst.app.roaddamagerecorder.model.weather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Item(
    val baseData : Int,
    val baseTime : Int,
    val category : String,
    val fcstTime : String,
    val fcstValue : String
)