package com.moutamid.quickdrop.walkthrough;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.moutamid.quickdrop.ModuleScreen;
import com.moutamid.quickdrop.R;

public class WalkthroughScreens extends AppCompatActivity {

    public static ViewPager viewPager;
    SlideViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_screens);
        viewPager=findViewById(R.id.viewpager);
        adapter=new SlideViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(isOpenAlread())
        {
            Intent intent=new Intent(WalkthroughScreens.this, ModuleScreen.class);
            startActivity(intent);
        }
        else
        {
            SharedPreferences.Editor editor =getSharedPreferences("slide", MODE_PRIVATE).edit();
            editor.putBoolean("slide",true);
            editor.commit();
        }
    }

    private boolean isOpenAlread() {
        SharedPreferences sharedPreferences=getSharedPreferences("slide", MODE_PRIVATE);
        boolean result= sharedPreferences.getBoolean("slide", false);
        return result;
    }
}
