package com.example.found;


import java.util.ArrayList;
import java.util.List;

import util.CanChatUdpReceiver;
import util.CanChatUdpSend;
import util.Udp;

import com.example.fragment.FunFragment;
import com.example.fragment.InfoFragment;
import com.example.fragment.ListFragment;
import com.example.view.CubePageTransformer;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.GestureDetector;;

public class MainActivity extends FragmentActivity implements OnClickListener{

	private final static int LEFT = 1;
	private static final int RIGHT = 0;
	
	public static String localHostIp= null;
	public static String ipToAll = null;
	public final static int GET_IP_FAILURE = 1;
	
	/**定义手势检测实例*/
//	 public static GestureDetector detector;
	 /**做标签，记录当前是哪个fragment*/
	 public int MARK=0;
	 /**定义手势两点之间的最小距离*/
	 private static final int FLING_MIN_DISTANCE = 100;   
	 private static final int FLING_MIN_VELOCITY = 0;

	 private ViewPager mViewPager;
	 private ArrayList<Fragment> mFragments;
	 //主界面菜单
     private ImageView listImage;
     private ImageView funImage;
     private ImageView infoImage;

     //fragement
     private Fragment myFragment;
     private ListFragment listFragment =new ListFragment();
     private InfoFragment infoFragment =new InfoFragment();
     private FunFragment funFragment =new FunFragment();
    
     //接收信息服务器
     private CanChatUdpReceiver udpAllReceiver;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        //创建手势检测器
//        detector=new GestureDetector(this);//屏蔽手势检测
        
        new Thread(){
            public void run(){
                boolean flag = true;
                while(localHostIp == null || ipToAll==null) {
                    try {
                        localHostIp = Udp.getIp();//获取局域网分配IP
                        ipToAll = Udp.getBroadcast();//获取广播IP
                        //如果获取ip失败，通过Toast提示
                        if (localHostIp == null || ipToAll==null) {
                            if(flag) {
                                Message msg = new Message();
                                msg.what = GET_IP_FAILURE;
                                handler.sendMessage(msg);
                                flag = false;
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(e.getMessage(), "获取ip失败");

                    }
                }
            }
        }.start();
        
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        //主菜单界面 菜单切换
        listImage = (ImageView)findViewById(R.id.listImage);
        funImage = (ImageView)findViewById(R.id.funImage);
        infoImage = (ImageView)findViewById(R.id.infoImage);
        LinearLayout listMenu = (LinearLayout)findViewById(R.id.listMenu);
        LinearLayout funMenu = (LinearLayout)findViewById(R.id.funMenu);
        LinearLayout infoMenu = (LinearLayout)findViewById(R.id.infoMenu);
        listMenu.setOnClickListener(this);
        funMenu.setOnClickListener(this);
        infoMenu.setOnClickListener(this);
        
        mFragments = new ArrayList<Fragment>();
        mFragments.add(listFragment);
        mFragments.add(funFragment);
        mFragments.add(infoFragment);
        
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(final int arg0) {
				final int currentItem = mViewPager.getCurrentItem();
				setTab(currentItem);
			}
			
			@Override
			public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(final int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mViewPager.setPageTransformer(true,new CubePageTransformer());
	    mViewPager.setCurrentItem(0);//设置当前显示标签页为第一页 
        //设置默认fragment
//        setFragment(listFragment);
        
      //启动接受服务器//当获取到IP时启动
        new Thread(){
            public void run(){
                boolean flag = true;
                while(flag){
                    if(localHostIp != null && ipToAll!=null) {
                        if (udpAllReceiver == null) {
                            //启动群接受服务
                            Log.e(""+ipToAll,""+localHostIp);
                            udpAllReceiver = new CanChatUdpReceiver(MainActivity.this, Udp.PORT_ALL);
                            udpAllReceiver.start();
                            flag = false;
                        }
                    }
                }
            }
        }.start();
        
      //启动线程发送测试在线人数
        new Thread(new checkedOnline()).start();
    }
    
    //启动线程发送测试在线人数
    class checkedOnline implements Runnable{
        public void run(){
            try {
                while (true) {
                    String userName = getUserName();
                    int imageId = getUserImageId();
                    String data = getData(userName, imageId);//分装数据处理
                    new Thread(new CanChatUdpSend(data, ipToAll, Udp.PORT_ALL)).start();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    
    //封装数据
    private String getData(String userName,int imageId){
        String data =Udp.CHECKED_CODE + userName+"##"+imageId;
        return data;
    }

    //获取用户昵称
    public String getUserName(){
        SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userName = sp.getString("userName","");
        return userName;
    }

    //获取用户头像
    public int getUserImageId(){
        SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int imageId = sp.getInt("userImageId",0);
        return imageId;
    }

  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*    
    //设置Fragment
    private void setFragment(Fragment fragment){
        //判断是否为空，则进行初始化
        if(myFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
//            transaction.add(R.id.mainFragment,fragment);
            myFragment = fragment;//传递参数值，显示的fragment
            transaction.commit();
        }else{
            if(myFragment != fragment){
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                //先影藏，然后判断是否添加过，没有则添加，有则显示
                if(fragment.isAdded()){
                    transaction.hide(myFragment).show(fragment);
                }else{
//                    transaction.hide(myFragment).add(R.id.mainFragment,fragment);
                }
                myFragment = fragment;//传递参数值，显示的fragment
                transaction.commit();
            }
        }
    }
*/
    //设置下方菜单viewPager用
    private void setTab(int selected){
    	if(selected == 0)
            listImage.setImageResource(R.drawable.skin_tab_icon_conversation_selected);
        else
            listImage.setImageResource(R.drawable.skin_tab_icon_conversation_normal);

        if(selected == 1)
            funImage.setImageResource(R.drawable.skin_tab_icon_plugin_selected);
        else
            funImage.setImageResource(R.drawable.skin_tab_icon_plugin_normal);

        if(selected == 2)
            infoImage.setImageResource(R.drawable.skin_tab_icon_contact_selected);
        else
            infoImage.setImageResource(R.drawable.skin_tab_icon_contact_normal);
    }
    //设置菜单选中状态
    private void setSelectedMenu(ImageView selected){
        if(selected == listImage)
            listImage.setImageResource(R.drawable.skin_tab_icon_conversation_selected);
        else
            listImage.setImageResource(R.drawable.skin_tab_icon_conversation_normal);

        if(selected == funImage)
            funImage.setImageResource(R.drawable.skin_tab_icon_plugin_selected);
        else
            funImage.setImageResource(R.drawable.skin_tab_icon_plugin_normal);

        if(selected == infoImage)
            infoImage.setImageResource(R.drawable.skin_tab_icon_contact_selected);
        else
            infoImage.setImageResource(R.drawable.skin_tab_icon_contact_normal);
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.listMenu:
                //设置菜单颜色图片
                setSelectedMenu(listImage);
                //设置Fragment
//                setFragment(listFragment);
                mViewPager.setCurrentItem(0);
                MARK = 0;

                break;
            case R.id.funMenu:
                //设置菜单颜色图片
                setSelectedMenu(funImage);
                //设置Fragment
//                setFragment(funFragment);
                mViewPager.setCurrentItem(1);
                MARK = 1;
                break;
            case R.id.infoMenu:
                //设置菜单颜色图片
                setSelectedMenu(infoImage);
                //设置Fragment
//                setFragment(infoFragment);
                mViewPager.setCurrentItem(2);
                MARK = 2;        
                break;
            default:
        }
    }
    //0右 1左
    private void switchFragment(int dir){
    	if(dir == RIGHT){
    		//向右滑动
    		switch(MARK){
    		case 0:
    			setSelectedMenu(funImage);
//    			setFragment(funFragment);
    			MARK = 1;
    			break;
    		case 1:
    			setSelectedMenu(infoImage);
//    			setFragment(infoFragment);
    			MARK = 2;
    			break;
    		case 2:
    			//不做任何操作
    			break;
    			default:break;
    		}
    	}
    	else if(dir == LEFT){
    		//向左滑动
    		switch(MARK){
    		case 0:
    			//不做任何操作
    			break;
    		case 1:
    			setSelectedMenu(listImage);
//    			setFragment(listFragment);
    			MARK = 0;
    			break;
    		case 2:
    			setSelectedMenu(funImage);
//    			setFragment(funFragment);
    			MARK = 1;
    			break;
    			default:break;
    		}
    	}
    }

    //获取IP失败提示
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == GET_IP_FAILURE){
                Toast.makeText(MainActivity.this, "获取Ip失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        }
    };
    
/*
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
*/
	/** 
     * 解决ScrollView后不执行左右移动监听事件OnGestureListener 
     * 在Activity中添加ScrollView实现滚动activity的效果后，activity的滑动效果却无法生效了 
     * 原因是因为activity没有处理滑动效果，重写以下方法即可解决。 
     */ 
/*
    @Override   
    public boolean dispatchTouchEvent(MotionEvent ev) {   
        detector.onTouchEvent(ev);   
        return super.dispatchTouchEvent(ev);   
    }   
    
	@Override   
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,   
            float velocityY) {   
        // TODO Auto-generated method stub   
         if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE    
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) { 
        	    switchFragment(RIGHT);
                // Fling left    
//                Toast.makeText(MainActivity.this, "向左手势", Toast.LENGTH_SHORT).show();    
            } else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE    
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) { 
            	switchFragment(LEFT);
                // Fling right    
//                Toast.makeText(MainActivity.this, "向右手势", Toast.LENGTH_SHORT).show();    
            }    
            return false;    
    }   
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//将该Activity上触碰事件交给GestureDetector处理
		return detector.onTouchEvent(event);
	}
*/
	
}
