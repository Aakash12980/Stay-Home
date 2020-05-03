package com.example.stayhome.adpaters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.stayhome.fragments.LoginFragment;
import com.example.stayhome.fragments.SignupFragment;

public class LoginViewPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public LoginViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0? "Login" : "Signup";
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0? new LoginFragment(): new SignupFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
