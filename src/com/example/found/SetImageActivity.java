package com.example.found;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.support.v4.app.FragmentActivity;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class SetImageActivity extends FragmentActivity {

    private int[] imageIds = new int[]{
            R.drawable.dml,
            R.drawable.dmk,
            R.drawable.dmn,
            R.drawable.dmm,
            R.drawable.head1,
            R.drawable.head2,
            R.drawable.head3,
            R.drawable.head4,
            R.drawable.head5,
            R.drawable.head6,
            R.drawable.head7,
            R.drawable.head8
    };

    private GridView myImageGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_headimage);

        myImageGridView = (GridView)findViewById(R.id.myImageGridView);
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        for(int i=0;i<imageIds.length;i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("headImage",imageIds[i]);
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.item_gridview_image,new String[]{"headImage"},new int[]{R.id.headImage});
        myImageGridView.setAdapter(adapter);

        myImageGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("userImageId",imageIds[position]);
                editor.commit();
                finish();
                overridePendingTransition(R.anim.stay_in,R.anim.slide_out_down);
            }
        });
    }
}
