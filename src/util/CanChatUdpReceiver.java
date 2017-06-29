package util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.found.MainActivity;
import com.example.found.R;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * 接受服务
 */
public class CanChatUdpReceiver extends Thread{

	public static final String MSG_FROM_ME = "MSG_FROM_ME"; 
	public static final String MSG_FROM_OTHER = "MSG_FROM_OTHER";
	//存储聊天记录
    public static List<MessageInfo> messageInfos = new ArrayList<MessageInfo>();

    public static List<ListContactInfo> listContactInfos = new ArrayList<ListContactInfo>();

    //存储聊天数据库
    private MsgSQLiteOpenHelper helper;
    //数据库表message操作实例化
    private MessageDao messageDao;

    private int port;
    //停止标志
    private boolean flag = true;

    private DatagramSocket da = null;

    private Context context;

    public CanChatUdpReceiver(Context context,int port){
        this.context = context;
        this.port = port;

        //context.deleteDatabase("message.db");//删除数据库初始化
        //创建数据库
        helper = new MsgSQLiteOpenHelper(context);
        //数据库表message操作实例化
        messageDao =new MessageDao(helper);

        List<MessageInfo> msgInfos = messageDao.findAll();
        if(msgInfos.size() > 50) {//提取一部分数据库内容
            for (int i = msgInfos.size()-40; i < msgInfos.size(); i++) {
                messageInfos.add(msgInfos.get(i));
            }
        }else{
            for (int i = 0; i < msgInfos.size(); i++) {
                messageInfos.add(msgInfos.get(i));
            }
        }

        //初始化列表中的群聊数据
        ListContactInfo listContactInfo = new ListContactInfo();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time =simpleDateFormat.format(calendar.getTime());

        listContactInfo.setListName("局域网群聊");
        listContactInfo.setListImage(R.drawable.dml);
        listContactInfo.setListIp(MainActivity.ipToAll);
        listContactInfo.setListTime(time);
        listContactInfo.setListUnRead("1");

        if(listContactInfos.size()< 1)
            listContactInfos.add(listContactInfo);
    }

    public void run(){
        try{
            if(da == null)
                da = new DatagramSocket(port);
            while (flag){
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf,buf.length);
                da.receive(dp);
                String ip = dp.getAddress().getHostAddress();
                String data = new String(dp.getData(),0,dp.getLength());

                //数据分析开始 判断数据类型

                if(data.contains(Udp.CHECKED_CODE)){
                    //判断在线人数部分//判断在线人数部分
                    //取出 剔除判断码部分 取出列表信息
                    String listInfoData = data.substring(Udp.CHECKED_CODE.length(),data.length());
                    //数据解析提取
                    boolean isAdd = true;
                    ListContactInfo newListContactInfo = getListContactInfo(listInfoData);
                    for (int i =0;i<listContactInfos.size();i++){
                        ListContactInfo listContactInfo = listContactInfos.get(i);
                        if(listContactInfo.getListIp().equals(ip)){

                            listContactInfo.setListName(newListContactInfo.getListName());
                            listContactInfo.setListName(newListContactInfo.getListName());
                            listContactInfo.setListImage(newListContactInfo.getListImage());

                            isAdd =false;
                            break;
                        }
                    }

                    //联系人列表数据加入
                    if(isAdd) {
                        newListContactInfo.setListIp(ip);
                        listContactInfos.add(newListContactInfo);
                        Log.e("打印提示数据：", "列表统计数量：" + listContactInfos.size()+" ip:" + ip);
                    }

                }else{
                    //消息数据解析部分
                    MessageInfo messageInfo =getMessageInfo(data);
                    messageInfo.setUserIp(ip);
                    messageInfos.add(messageInfo);
                    //发送新消息广播
                    Intent intent = new Intent();
                    intent.putExtra("listName",messageInfo.getListName());
                    intent.putExtra("userName",messageInfo.getUserName());
                    intent.putExtra("userIp",messageInfo.getUserIp());
                    intent.putExtra("imageId",messageInfo.getImageId());
                    intent.putExtra("msgBody",messageInfo.getMsgBody());
                    intent.putExtra("receTime",messageInfo.getReceTime());
                    /*
                     * 接收到来自己的广播消息时做标记
                     
                    if(messageInfo.getUserIp().equals(MainActivity.localHostIp))
                    	intent.putExtra("flag",MSG_FROM_ME);
                    else
                    	intent.putExtra("flag",MSG_FROM_OTHER);
                    */
                    intent.setAction("com.ADD_VIEW");
                    context.sendBroadcast(intent);

                    //保存数据到数据库
                   
                    if(!messageInfo.getUserIp().equals(MainActivity.localHostIp))
                    	saveMessageToSQL(messageInfo);
                }

            }
        }catch(Exception e){
            Log.i(e.getMessage(),"服务器挂了");
        }finally {
            try{
                if(da !=null)
                    da.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //消息数据处理
    private MessageInfo getMessageInfo(String data){
        String[] datas = data.split("##");
        MessageInfo messageInfo = new MessageInfo();

        messageInfo.setListName(datas[0]);
        messageInfo.setUserName(datas[1]);
        messageInfo.setImageId(Integer.parseInt(datas[2]));
        messageInfo.setMsgBody(datas[3]);
        messageInfo.setReceTime(datas[4]);

        return messageInfo;
    }

    //联系人列表数据处理
    private ListContactInfo getListContactInfo(String data){
        //解析数据data
        String[] datas = data.split("##");

        //
        ListContactInfo listContactInfo = new ListContactInfo();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time =simpleDateFormat.format(calendar.getTime());

        listContactInfo.setListTime(time);
        listContactInfo.setListUnRead("1");
        listContactInfo.setListName(datas[0]);
        listContactInfo.setListImage(Integer.parseInt(datas[1]));

        return listContactInfo;
    }



    //保存消息数据到数据库
    private void saveMessageToSQL(MessageInfo messageInfo){
        try {
            String userName = messageInfo.getUserName();
            String listName = messageInfo.getListName();
            String userIp = messageInfo.getUserIp();
            int imageId = messageInfo.getImageId();
            String msgBody = messageInfo.getMsgBody();
            String receTime = messageInfo.getReceTime();

            messageDao.add(listName,userName,userIp,imageId,msgBody,receTime);
            Log.e("数据库操作提示：", "保存成功");
        }catch (Exception e){
            Log.d(e.getMessage(),"保存失败");
        }
    }
}
