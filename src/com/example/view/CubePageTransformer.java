package com.example.view;
import android.support.v4.view.ViewPager;  
import android.view.View;  
  
public class CubePageTransformer implements ViewPager.PageTransformer  
{  
    public void transformPage(View view, float position)  
    {  
            //���0~-90��,�ұ�90~0��,  
            //���x 0~-width���ұ�x width~0��  
        if (position < -1)  
        {  
  
        } else if (position <= 1) // aҳ������bҳ �� aҳ�� 0.0 ~ -1 ��bҳ��1 ~ 0.0  
        { // [-1,1]  
            if (position < 0)//���������ҳ��  
            {  
                view.setPivotX(view.getMeasuredWidth());  
                view.setRotationY(position*90);  
            } else//�������ұ�ҳ��  
            {  
                view.setPivotX(0);  
                view.setRotationY(position*90);  
            }  
  
        } else  
        { // (1,+Infinity]  
              
        }  
    }  
}  
