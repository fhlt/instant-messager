package com.example.view;
import android.support.v4.view.ViewPager;  
import android.view.View;  
  
public class CubePageTransformer implements ViewPager.PageTransformer  
{  
    public void transformPage(View view, float position)  
    {  
            //×ó±ß0~-90¶È,ÓÒ±ß90~0¶È,  
            //×ó±ßx 0~-width£¬ÓÒ±ßx width~0£»  
        if (position < -1)  
        {  
  
        } else if (position <= 1) // aÒ³»¬¶¯ÖÁbÒ³ £» aÒ³´Ó 0.0 ~ -1 £»bÒ³´Ó1 ~ 0.0  
        { // [-1,1]  
            if (position < 0)//»¬¶¯ÖÐ×ó±ßÒ³Ãæ  
            {  
                view.setPivotX(view.getMeasuredWidth());  
                view.setRotationY(position*90);  
            } else//»¬¶¯ÖÐÓÒ±ßÒ³Ãæ  
            {  
                view.setPivotX(0);  
                view.setRotationY(position*90);  
            }  
  
        } else  
        { // (1,+Infinity]  
              
        }  
    }  
}  
