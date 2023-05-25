package com.example.sueobmwodeudji;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sueobmwodeudji.databinding.ActivityMainBinding;
import com.example.sueobmwodeudji.ui.CommunityFragment;
import com.example.sueobmwodeudji.ui.HomeFragment;
import com.example.sueobmwodeudji.ui.RatingsFragment;
import com.example.sueobmwodeudji.ui.SettingsFragment;
import com.example.sueobmwodeudji.ui.TimeTableFragment;

import java.sql.Time;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private long backKeyPressedTime = 0L;

    @Override
    public void onBackPressed() {
//        안드로이드에서 기본으로 제공하는 뒤로가기
//        super.onBackPressed();

//      2000 = 2초
        if (System.currentTimeMillis() - backKeyPressedTime >= 2000 ) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        
//      2초 이내에 뒤로가기 버튼을 한번 더 누르면 종료
        if (System.currentTimeMillis() - backKeyPressedTime < 2000 ) {
            finish(); // 액티비티 종료
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolBar.mainToolBar;
        setSupportActionBar(toolbar);

        BottomNavBar();
    }

    // 네비게이션바 클릭시 프래크먼트 이동
    private void BottomNavBar() {
        // 기본화면 설정(홈 화면)
        getSupportFragmentManager().beginTransaction().replace(R.id.containers, new HomeFragment()).commit();
        getSupportActionBar().setTitle("홈");

        // 화면 바뀜
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containers, new HomeFragment())
                            .commit();
                    getSupportActionBar().setTitle("홈");
                    return true;
                case R.id.navigation_time_table:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containers,  new TimeTableFragment())
                            .commit();
                    getSupportActionBar().setTitle("시간표");
                    return true;
                case R.id.navigation_community:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containers,  new CommunityFragment())
                            .commit();
                    return true;
                case R.id.navigation_ratings:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containers, new RatingsFragment())
                            .commit();
                    return true;
                case R.id.navigation_settings:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containers, new SettingsFragment())
                            .commit();
                    getSupportActionBar().setTitle("설정");
                    return true;
            }
            return false;
        });
    }

}