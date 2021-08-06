package kst.app.roaddamagerecorder.model.weather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Response(
    val header : Header,
    val body : Body
)