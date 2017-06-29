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

    //��̬ע��㲥�����ߣ������ڲ���Ҫ���徲̬�࣬��ʹ�ڲ������ ʹ�õ��ⲿ���Ա���������������Ǿ�̬���鷳
    private NewMsgBroadcastRecevier newMsgBroadcastRecevier;

    //ͷ���б�
    private int[] imageIds = new int[]{
            R.drawable.dml,
            R.drawable.dmk,
            R.drawable.dmn,
            R.drawable.dmm
    };

    //scrollview�������촰��
    private ScrollView myScrollView;
    //scrollview�����Բ���
    private LinearLayout chatMsgBoxLL;
    //�б����Ʊ�����ʾ
    private TextView chatTitleName;
    //���ͱ༭��
    private EditText chatWithText;

    //Ҫ���͵���ip
    private String userIPToSend;


    //�����̴߳������̵߳Ĳ���
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

    //����scrollview������Ϣʱ��������ײ�
    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            int off = chatMsgBoxLL.getMeasuredHeight() - myScrollView.getHeight();
            if (off > 0) {
                myScrollView.scrollTo(0, off);
            }
        }
    };

    //���ݿⶨ��

    private MsgSQLiteOpenHelper helper;
    private MessageDao messageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_chat);

        //��̬ע��㲥�����ߣ������ڲ���Ҫ���徲̬�࣬��ʹ�ڲ������ ʹ�õ��ⲿ���Ա���������������Ǿ�̬���鷳
        newMsgBroadcastRecevier = new NewMsgBroadcastRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ADD_VIEW");
        this.registerReceiver(newMsgBroadcastRecevier,filter);

        helper = new MsgSQLiteOpenHelper(this);
        messageDao = new MessageDao(helper);

        //�����������
        myScrollView = (ScrollView)findViewById(R.id.chatScrollView);
        chatMsgBoxLL = (LinearLayout)findViewById(R.id.chatMsgBoxLL);
        chatWithText = (EditText)findViewById(R.id.chatWithText);
        chatTitleName = (TextView)findViewById(R.id.chatTitleName);

        //���ذ�ť�ͷ��Ͱ�ť����
        Button chatSend = (Button)findViewById(R.id.chatSendBtn);
        Button chatBack = (Button)findViewById(R.id.chatBack);
        chatSend.setOnClickListener(this);
        chatBack.setOnClickListener(this);

        //������ͼ��Ϣ��ȡ����ȡ�����б�����ip���ڷ�����Ϣ
        Intent intent = getIntent();
        //�б���
        String listName = intent.getStringExtra("listName");
        chatTitleName.setText(listName);
        //��Ӧip
        userIPToSend = intent.getStringExtra("listIp");

        //��ʼ��������������
        initMessageView();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            //������Ϣ
            case R.id.chatSendBtn:
                try{
                    //UdpЭ�鷢����Ϣ����
                    String data = chatWithText.getText().toString();
                    //�ж������Ƿ�Ϊ�գ�Ϊ�ղ��跢��
                    if(data.equals("")) {
                        Toast.makeText(this,"���ݲ���Ϊ��",Toast.LENGTH_SHORT).show();
                        break;
                    }

                    //���ݷ�װ����
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    String time = format.format(calendar.getTime());
                    String listName =chatTitleName.getText().toString();//�б���
                    String userName = getUserName();//�ǳ�
                    int imageId = getUserImageId();

                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setListName(listName);
                    messageInfo.setUserName(userName);
                    messageInfo.setImageId(imageId);
                    messageInfo.setReceTime(time);
                    messageInfo.setMsgBody(data);
                    //��ӷ�����Ϣ���������
                    addSendMessage(messageInfo);
                    //��ӵ����ݿ�
                    /*
                     * �Լ����͵���Ϣ���ٽ���
                     */
//                    CanChatUdpReceiver.messageInfos.add(messageInfo);
                    //��ӵ����ݿ�
                    
                    messageDao.add(listName, userName,"",imageId,data,time);

                    //�ж�Ҫ���͵��б��� ��ΪȺ��ʱ ���䣬����ΪȺ��ʱ����Ϊ�б���Ϊ�û���
                    if(!listName.equals("������Ⱥ��"))
                        listName = userName;
                    String dataToSend =listName +"##" + userName + "##" + imageId +"##" + data +"##" + time;
                    //������Ϣ�����
                    new Thread(new CanChatUdpSend(dataToSend, userIPToSend, Udp.PORT_ALL)).start();
                    //������Ϣ�����EditText�༭��
                    chatWithText.setText("");

                }catch(Exception e){
                    Log.e(e.getMessage(),"��Ϣ����ʧ��");
                    throw new RuntimeException(e);
                }
                break;

            case R.id.chatBack:
                //�ر�������棬����������
                finish();
                overridePendingTransition(R.anim.stay_in,R.anim.slide_out_down);
                break;
            default:
        }
    }

    //��ȡ�û��ǳ�
    public String getUserName(){
        SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userName = sp.getString("userName","");
        return userName;
    }

    //��ȡ�û�ͷ��
    public int getUserImageId(){
        SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int imageId = sp.getInt("userImageId", 0);
        return imageId;
    }

    //��ӷ��͵���Ϣ
    public void addSendMessage(MessageInfo msgInfo){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_send_view, null);
        TextView userMsg  = (TextView)view.findViewById(R.id.userMsg);
        TextView userName = (TextView)view.findViewById(R.id.userName);
        ImageView userImage = (ImageView)view.findViewById(R.id.userImage);

        userName.setText("��");
        userMsg.setText(msgInfo.getMsgBody());
        userImage.setImageResource(msgInfo.getImageId());

        chatMsgBoxLL.addView(view);
        //�ƶ������
        handler.post(mScrollToBottom);
    }

    //��ӽ��ܵ���Ϣ
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
	        //�ƶ������
	        handler.post(mScrollToBottom);
       }
    }

    //��ʼ��������������
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
            //���͸����̼߳�������
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
