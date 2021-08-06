package kst.app.roaddamagerecorder.model.weather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WeatherSend(
    @SerializedName("ServiceKey")
    @Expose
    var serviceKey: String? = null,

    @SerializedName("pageNo")
    @Expose
    val pageNo: String? = null,

    @SerializedName("numOfRows")
    @Expose
    val numOfRows: String? = null,

    @SerializedName("dataType")
    @Expose
    val dataType: String? = null,

    @SerializedName("base_date")
    @Expose
    val baseDate: String? = null,

    @SerializedName("base_time")
    @Expose
    val baseTime: String? = null,

    @SerializedName("nx")
    @Expose
    val nX: String? = null,

    @SerializedName("ny")
    @Expose
    val nY: String? = null,
)