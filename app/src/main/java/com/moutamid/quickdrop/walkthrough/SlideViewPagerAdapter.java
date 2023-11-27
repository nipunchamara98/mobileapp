package com.moutamid.quickdrop.walkthrough;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.moutamid.quickdrop.ModuleScreen;
import com.moutamid.quickdrop.R;


public class SlideViewPagerAdapter extends PagerAdapter {

    Context ctx;

    public SlideViewPagerAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater= (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.slide_screen,container,false);
        ImageView logo1 =view.findViewById(R.id.logo1);
        ImageView sel1 =view.findViewById(R.id.sel1);
        ImageView sel2 =view.findViewById(R.id.sel2);
        ImageView sel3 =view.findViewById(R.id.sel3);
        TextView skip = view.findViewById(R.id.skip);

        TextView title =view.findViewById(R.id.titles);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.startActivity(new Intent(ctx, ModuleScreen.class));
            }
        });

        if (position > 2){
            ctx.startActivity(new Intent(ctx, ModuleScreen.class));
        }

        switch (position)
        {
            case 0:
                logo1.setImageResource(R.drawable.splash1);
                sel1.setImageResource(R.drawable.selected);
                sel2.setImageResource(R.drawable.unselected);
                sel3.setImageResource(R.drawable.unselected);

                title.setText("Accept a Job");
                //back.setVisibility(View.GONE);
                break;

            case 1:
                logo1.setImageResource(R.drawable.splash2);
                sel1.setImageResource(R.drawable.unselected);
                sel2.setImageResource(R.drawable.selected);
                sel3.setImageResource(R.drawable.unselected);

                title.setText("Tracking Realtime");
                //back.setVisibility(View.INVISIBLE);
              //  next.setVisibility(View.INVISIBLE);
                break;

            case 2:
                logo1.setImageResource(R.drawable.splash3);
                sel1.setImageResource(R.drawable.unselected);
                sel2.setImageResource(R.drawable.unselected);
                sel3.setImageResource(R.drawable.selected);

                title.setText("Earn Money");
               // back.setVisibility(View.INVISIBLE);
                //next.setVisibility(View.GONE);
                break;
        }



        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
