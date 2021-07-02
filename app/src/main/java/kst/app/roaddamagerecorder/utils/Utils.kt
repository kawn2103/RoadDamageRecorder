package kst.app.roaddamagerecorder.utils

import android.util.Log
import kst.app.roaddamagerecorder.asyncTask.NetWorkCheckAsyncTask
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern

object Utils {

    val baseUrl = "http://118.67.150.142/"

    //아이디 유효성 체크
    fun idCheck(id: String): Boolean {
        //네개의 값이 모두 true여야 아이디 조건에 맞음
        var notIncludeKorean = true
        var notIncludeSpace = true
        val notIncludeBigEnglish = true
        var minLength8 = false
        var includeNumber = false
        if (id.length > 5) { //최소길이 검사
            minLength8 = true
            for (i in id.indices) {
                if (id.substring(i, i + 1) == " ") { //공백 검사
                    notIncludeSpace = false
                    break
                }
                if (Pattern.matches("^[ㄱ-ㅎ가-힣]*$", id.substring(i, i + 1))) { //한글 포함 검사
                    notIncludeKorean = false
                }
                /*if (Pattern.matches("^[A-Z]*$", id.substring(i, i + 1))) {//영어 대문자 포함 검사
                    notIncludeBigEnglish = false;
                }*/
                if (Pattern.matches("^[0-9]*$", id.substring(i, i + 1))) { //숫자 포함 검사
                    includeNumber = true
                }
            }
        }
        //notIncludeKorean && notIncludeSpace && minLength8 && includeNumber && notIncludeBigEnglish
        return notIncludeKorean && notIncludeSpace && minLength8 && includeNumber
    }

    //현재 네트워크 상태 체크
    fun networkCheck(): Boolean {

        //인터넷 체크
        var netCheck = false
        val netWorkCheckAsyncTask = NetWorkCheckAsyncTask()
        try {
            netCheck = netWorkCheckAsyncTask.execute().get(1000, TimeUnit.MILLISECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
        Log.d("gwan_netcheck", "net_connect_check >>>>>> $netCheck")
        return netCheck
    }


}