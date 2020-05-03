package com.example.stayhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.stayhome.adpaters.LoginViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class LoginSignup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        ViewPager viewPager = (ViewPager) findViewById(R.id.login_signup_viewpager);
        LoginViewPagerAdapter adapter = new LoginViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.login_signup_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
