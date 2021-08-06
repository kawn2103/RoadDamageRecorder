package kst.app.roaddamagerecorder

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import kst.app.roaddamagerecorder.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){

        //툴바 설정
        binding.toolBar.navigationIcon = null
        setSupportActionBar(binding.toolBar)
        //앱바 그림자 제거
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(
            IntArray(0),
            ObjectAnimator.ofFloat(binding.appBarLayout, "elevation", 0f)
        )
        binding.appBarLayout.stateListAnimator = stateListAnimator
        //드로워 레이아웃 설정
        val drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerToggle.isDrawerIndicatorEnabled = false
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        //네비게이션 설정
        val naviHeaderView = binding.navigationView.getHeaderView(0)
        binding.navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                /*R.id.nav_sub_reward ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_reward")
                    true
                }
                R.id.nav_sub_guide_line ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_guide_line")
                    true
                }
                R.id.nav_sub_my_info ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_my_info")
                    true
                }
                R.id.nav_sub_noti ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_noti")
                    true
                }*/
                R.id.nav_sub_sync ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_sync")
                    true
                }
                R.id.nav_sub_logout ->{
                    Log.d("gwan2103_main","menuClick ====> nav_sub_logout")
                    true
                }
                else -> false
            }
        }
        //네비게이션 뷰 화면 크기 조절
        val observer = binding.navigationView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            //네비게이션 뷰 전체 화면 비율로 키우기
            val params = binding.navigationView.layoutParams as DrawerLayout.LayoutParams
            params.width = binding.drawerLayout.width * 2 / 3
            val params1 = naviHeaderView.layoutParams as LinearLayout.LayoutParams
            params1.height = binding.navigationView.height / 10
            binding.navigationView.layoutParams = params
            naviHeaderView.layoutParams = params1
        }

        binding.includeLayout.waveBlue.startRippleAnimation()

        binding.includeLayout.waveBlue.setOnClickListener {
            Log.d("gwan2103_main","촬영 버튼을 눌렀어요")
        }
    }
}