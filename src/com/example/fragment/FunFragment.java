package com.example.fragment;



import com.example.found.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FunFragment extends Fragment {

	private Button netImageSeeBtn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_fun, container, false);

		netImageSeeBtn = (Button) view.findViewById(R.id.netImageSeeBtn);
//		netImageSeeBtn.setOnClickListener(this);

		return view;
	}


}
