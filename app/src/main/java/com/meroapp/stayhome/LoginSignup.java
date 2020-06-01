package com.meroapp.stayhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.meroapp.stayhome.adpaters.LoginViewPagerAdapter;
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
    }
}
