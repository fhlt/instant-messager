package com.example.fragment;

import com.example.found.MainActivity;
import com.example.found.R;
import com.example.found.SetImageActivity;
import com.example.found.SetNameActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class InfoFragment extends Fragment implements View.OnClickListener{

	private TextView userName;
	private TextView hostIp;
	private ImageView userImage;
	private Button setUserNameBtn;
	private Button setImageBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_info, container, false);

		userName = (TextView) view.findViewById(R.id.userName);
		hostIp = (TextView) view.findViewById(R.id.hostIp);
		userImage = (ImageView) view.findViewById(R.id.userImage);

		setUserNameBtn = (Button) view.findViewById(R.id.setUserNameBtn);
		setImageBtn = (Button) view.findViewById(R.id.setImageBtn);
		
		setUserNameBtn.setOnClickListener(this);
		setImageBtn.setOnClickListener(this);

		userName.setText(android.os.Build.MODEL);//设置默认名称
		saveFirstName();//保存默认名称
		hostIp.setText(MainActivity.localHostIp);

		return view;
	}
	private void saveFirstName(){
		SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putString("userName", android.os.Build.MODEL);
	    editor.apply();
	}
	
	//设置昵称
    public void setUserName(){
        SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sp.getString("userName","");
        userName.setText(name);
    }

    //设置头像
    public void setUserImage(){
        SharedPreferences sp = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int imageId = sp.getInt("userImageId",0);
        userImage.setImageResource(imageId);
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.setUserNameBtn:
			getActivity().startActivityForResult(new Intent(this.getActivity(), SetNameActivity.class), 0);
            getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			break;
		case R.id.setImageBtn:
			getActivity().startActivityForResult(new Intent(this.getActivity(), SetImageActivity.class), 0);
            getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			break;
		default:break;
		}
	}
	 @Override
	    public void onResume(){
	        super.onResume();
	        //设置昵称
	        setUserName();
	        //设置头像
	        setUserImage();
	    }

}
