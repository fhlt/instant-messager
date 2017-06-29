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
                //�����ǳ�
                String name = username.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(this, "�ָ�Ĭ������", Toast.LENGTH_SHORT).show();
                    name = android.os.Build.MODEL.toString();
                }
                SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("userName", name);
                editor.apply();

                Toast.makeText(this,"����ɹ�",Toast.LENGTH_SHORT).show();

                break;
            case R.id.backBtn:
                finish();
                //��һ��activity��ת����һ��activityʱ�Ķ���
                overridePendingTransition(R.anim.stay_in,R.anim.slide_out_down);
                break;
            default:
        }
    }
}
