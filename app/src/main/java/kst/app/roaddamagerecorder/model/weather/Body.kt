package kst.app.roaddamagerecorder.model.weather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Body(
    val dataType : String,
    val items : Items
)