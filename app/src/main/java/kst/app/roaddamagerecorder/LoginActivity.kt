package kst.app.roaddamagerecorder

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kst.app.roaddamagerecorder.databinding.ActivityLoginBinding
import kst.app.roaddamagerecorder.model.login.LoginRoot
import kst.app.roaddamagerecorder.model.login.LoginSend
import kst.app.roaddamagerecorder.retrofit2.RetrofitInstance.RetrofitInstanceObject.getRetrofitService
import kst.app.roaddamagerecorder.retrofit2.RetrofitService
import kst.app.roaddamagerecorder.utils.Utils.idCheck
import kst.app.roaddamagerecorder.utils.Utils.networkCheck
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    //바인딩
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //화면 바인딩
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){

        binding.idEt.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.pwEt.imeOptions = EditorInfo.IME_ACTION_DONE

        // 자동 로그인 버튼 변경 이벤트 리스너
        binding.autoLoginBt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.autoLoginBt.setBackgroundResource(R.drawable.check_bt_able)
                getSharedPreferences("Login", Context.MODE_PRIVATE).edit {
                    putString("autoLogin", "check")
                    apply()
                }
            } else {
                binding.autoLoginBt.setBackgroundResource(R.drawable.check_bt_disable)
                getSharedPreferences("Login", Context.MODE_PRIVATE).edit {
                    putString("autoLogin", "check")
                    apply()
                }
            }
        }

        binding.loginBt.setOnClickListener {
            binding.notiTv.text = ""
            if (binding.idEt.text.isEmpty()){
                binding.notiTv.text = "아이디를 입력해 주세요"
                binding.idEt.requestFocus()
                return@setOnClickListener
            }

            if (binding.pwEt.text.isEmpty()){
                binding.notiTv.text = "비밀번호를 입력해 주세요"
                binding.pwEt.requestFocus()
                return@setOnClickListener
            }

            if (!idCheck(binding.idEt.text.toString().replace(" ", ""))){
                Toast.makeText(this, "아이디 형식이 일치 하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (networkCheck()) {
                logInRetrofit2()
            } else {
                Toast.makeText(applicationContext, "네트워크 환경을 확인해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logInRetrofit2(){
        //서버로 전송 할 데이터 셋
        var retrofitService : RetrofitService = getRetrofitService()

        //서버와 데이터를 주고 받을 Call 선언 Call<받는데이터> retrofitService.login(보내는데이터)
        var call: Call<LoginRoot> = retrofitService.login(LoginSend(
            binding.idEt.text.toString(),
            binding.pwEt.text.toString()
        ))

        call.enqueue(object : Callback<LoginRoot> {
            override fun onFailure(call: Call<LoginRoot>, t: Throwable) {
                Log.d("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<LoginRoot>,
                response: Response<LoginRoot>
            ) {
                Log.d("DEBUG : ", response.body().toString())

                val data = response.body()

                val code = data!!.code
                val msg = data!!.message

                Log.d("gwan_2103", "code >>>>>>>>>>>> $code")
                Log.d("gwan_2103", "msg >>>>>>>>>>>> $msg")

            }
        })
    }
}