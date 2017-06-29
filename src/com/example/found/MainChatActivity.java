package com.example.found;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import util.CanChatUdpReceiver;
import util.CanChatUdpSend;
import util.MessageDao;
import util.MessageInfo;
import util.MsgSQLiteOpenHelper;
import util.Udp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainChatActivity extends FragmentActivity implements View.OnClickListener{

    //动态注册广播接受者，避免内部类要定义静态类，而使内部类的中 使用的外部类成员变量方法都必需是静态的麻烦
    private NewMsgBroadcastRecevier newMsgBroadcastRecevier;

    //头像列表
    private int[] imageIds = new int[]{
            R.drawable.dml,
            R.drawable.dmk,
            R.drawable.dmn,
            R.drawable.dmm
    };

    //scrollview滚动聊天窗口
    private ScrollView myScrollView;
    //scrollview内线性布局
    private LinearLayout chatMsgBoxLL;
    //列表名称标题显示
    private TextView chatTitleName;
    //发送编辑框
    private EditText chatWithText;

    //要发送的用ip
    private String userIPToSend;


    //传递线程处理到主线程的参数
    private static final int ADD_VIEW = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == ADD_VIEW){
                MessageInfo messageInfo = (MessageInfo)msg.obj;
                String listName = messageInfo.getListName();
                if(listName.equals(chatTitleName.getText().toString())) {
                    addReceMessage(messageInfo);
                }
            }
        }
    };

    //用于scrollview接受消息时下拉到最底部
    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            int off = chatMsgBoxLL.getMeasuredHeight() - myScrollView.getHeight();
            if (off > 0) {
                myScrollView.scrollTo(0, off);
            }
        }
    };

    //数据库定义

    private MsgSQLiteOpenHelper helper;
    private MessageDao messageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_chat);

        //动态注册广播接受者，避免内部类要定义静态类，而使内部类的中 使用的外部类成员变量方法都必需是静态的麻烦
        newMsgBroadcastRecevier = new NewMsgBroadcastRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ADD_VIEW");
        this.registerReceiver(newMsgBroadcastRecevier,filter);

        helper = new MsgSQLiteOpenHelper(this);
        messageDao = new MessageDao(helper);

        //基本组件定义
        myScrollView = (ScrollView)findViewById(R.id.chatScrollView);
        chatMsgBoxLL = (LinearLayout)findViewById(R.id.chatMsgBoxLL);
        chatWithText = (EditText)findViewById(R.id.chatWithText);
        chatTitleName = (TextView)findViewById(R.id.chatTitleName);

        //返回按钮和发送按钮设置
        Button chatSend = (Button)findViewById(R.id.chatSendBtn);
        Button chatBack = (Button)findViewById(R.id.chatBack);
        chatSend.setOnClickListener(this);
        chatBack.setOnClickListener(this);

        //启动意图信息获取，获取聊天列表名和ip用于发送消息
        Intent intent = getIntent();
        //列表名
        String listName = intent.getStringExtra("listName");
        chatTitleName.setText(listName);
        //对应ip
        userIPToSend = intent.getStringExtra("listIp");

        //初始化加载聊天数据
        initMessageView();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            //发送消息
            case R.id.chatSendBtn:
                try{
                    //Udp协议发送消息部分
                    String data = chatWithText.getText().toString();
                    //判断内容是否为空，为空不予发送
                    if(data.equals("")) {
                        Toast.makeText(this,"内容不能为空",Toast.LENGTH_SHORT).show();
                        break;
                    }

                    //数据分装部分
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    String time = format.format(calendar.getTime());
                    String listName =chatTitleName.getText().toString();//列表名
                    String userName = getUserName();//昵称
                    int imageId = getUserImageId();

                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setListName(listName);
                    messageInfo.setUserName(userName);
                    messageInfo.setImageId(imageId);
                    messageInfo.setReceTime(time);
                    messageInfo.setMsgBody(data);
                    //添加发送消息到聊天界面
                    addSendMessage(messageInfo);
                    //添加到数据库
                    /*
                     * 自己发送的消息不再接收
                     */
//                    CanChatUdpReceiver.messageInfos.add(messageInfo);
                    //添加到数据库
                    
                    messageDao.add(listName, userName,"",imageId,data,time);

                    //判断要发送的列表名 当为群聊时 不变，当不为群聊时，改为列表名为用户名
                    if(!listName.equals("局域网群聊"))
                        listName = userName;
                    String dataToSend =listName +"##" + userName + "##" + imageId +"##" + data +"##" + time;
                    //发送消息主语句
                    new Thread(new CanChatUdpSend(dataToSend, userIPToSend, Udp.PORT_ALL)).start();
                    //发送消息后清空EditText编辑框
                    chatWithText.setText("");

                }catch(Exception e){
                    Log.e(e.getMessage(),"消息发送失败");
                    throw new RuntimeException(e);
                }
                break;

            case R.id.chatBack:
                //关闭聊天界面，返回主界面
                finish();
                overridePendingTransition(R.anim.stay_in,R.anim.slide_out_down);
                break;
            default:
        }
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
        int imageId = sp.getInt("userImageId", 0);
        return imageId;
    }

    //添加发送的信息
    public void addSendMessage(MessageInfo msgInfo){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_send_view, null);
        TextView userMsg  = (TextView)view.findViewById(R.id.userMsg);
        TextView userName = (TextView)view.findViewById(R.id.userName);
        ImageView userImage = (ImageView)view.findViewById(R.id.userImage);

        userName.setText("我");
        userMsg.setText(msgInfo.getMsgBody());
        userImage.setImageResource(msgInfo.getImageId());

        chatMsgBoxLL.addView(view);
        //移动到最后
        handler.post(mScrollToBottom);
    }

    //添加接受的信息
    public void addReceMessage(MessageInfo msgInfo) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_rece_view, null);
        TextView userMsg  = (TextView)view.findViewById(R.id.userMsg);
        TextView userIp  = (TextView)view.findViewById(R.id.userIp);
        TextView userName = (TextView)view.findViewById(R.id.userName);
        ImageView userImage = (ImageView)view.findViewById(R.id.userImage);

       if(!msgInfo.getUserIp().equals(MainActivity.localHostIp)){
	        userIp.setText(msgInfo.getUserIp());
	        userName.setText(msgInfo.getUserName());
	        userMsg.setText(msgInfo.getMsgBody());
	        userImage.setImageResource(msgInfo.getImageId());
	
	        chatMsgBoxLL.addView(view);
	        //移动到最后
	        handler.post(mScrollToBottom);
       }
    }

    //初始化加载聊天数据
    public void initMessageView(){

        for(MessageInfo info :CanChatUdpReceiver.messageInfos){
        	
            String listName = info.getListName();
            if(listName.equals(chatTitleName.getText().toString())){
                String userName = info.getUserName();
                if(userName.equals(getUserName())){
                	addSendMessage(info);
                }else{
                    addReceMessage(info);
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(newMsgBroadcastRecevier);
        super.onDestroy();
    }

    
    public class NewMsgBroadcastRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setListName(intent.getStringExtra("listName"));
            messageInfo.setUserName(intent.getStringExtra("userName"));
            messageInfo.setUserIp(intent.getStringExtra("userIp"));
            messageInfo.setImageId(intent.getIntExtra("imageId", 0));
            messageInfo.setMsgBody(intent.getStringExtra("msgBody"));
            messageInfo.setReceTime(intent.getStringExtra("receTime"));
            //发送给主线程加载数据
            if(!intent.getStringExtra("userIp").equals(MainActivity.localHostIp))
            {
            	Message msg = new Message();
                msg.what = ADD_VIEW;
                msg.obj = messageInfo;
                handler.sendMessage(msg);
            }
        }
    }
}
