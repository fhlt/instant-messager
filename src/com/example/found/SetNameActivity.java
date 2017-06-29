package com.example.found;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetNameActivity extends FragmentActivity implements OnClickListener{

	private EditText username;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		 setContentView(R.layout.activity_set_username);

	        username = (EditText)findViewById(R.id.username);

	        Button saveBtn = (Button)findViewById(R.id.saveBtn);
	        Button backBtn = (Button)findViewById(R.id.backBtn);
	        saveBtn.setOnClickListener(this);
	        backBtn.setOnClickListener(this);

	        SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
	        String name = sp.getString("userName","");
	        username.setText(name);
	}
	
	@Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saveBtn:
                //储存昵称
                String name = username.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(this, "恢复默认名字", Toast.LENGTH_SHORT).show();
                    name = android.os.Build.MODEL.toString();
                }
                SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("userName", name);
                editor.apply();

                Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();

                break;
            case R.id.backBtn:
                finish();
                //从一个activity跳转到另一个activity时的动画
                overridePendingTransition(R.anim.stay_in,R.anim.slide_out_down);
                break;
            default:
        }
    }
}
