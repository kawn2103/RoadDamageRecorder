package kst.app.roaddamagerecorder.asyncTask

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class NetWorkCheckAsyncTask : AsyncTask<String?, Void?, Boolean>() {
    override fun onPreExecute() {
        super.onPreExecute()
        Log.d("gwan_net_AsyncTask", "onPreExecute")
        // doInBackground가 실행되기 전에 실행된다.
    }

    override fun doInBackground(vararg params: String?): Boolean {
        // 파라미터에는 AsyncTask의 Param(첫번째)의 영향을 받는다.
        // 사용자가 진행할 주요 작업을 수행하는 구간
        Log.d("gwan_net_AsyncTask", "doInBackground")
        val result = ""
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("www.google.com", 80), 2000)
                return true
            }
        } catch (e: IOException) {
            // Either we have a timeout or unreachable host or failed DNS lookup
            return false
        }
        // 리턴값은 AsyncTask의 Result(세번째)의 영향을 받는다.
    }

    override fun onPostExecute(result: Boolean) {
        // doInBackground가 작업을 마친 후 리턴된 값을 받으며 후속 작업을 한다.
        super.onPostExecute(result)
        Log.d("gwan_net_AsyncTask", "onPostExecute")
    }


}