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
 * ���ܷ���
 */
public class CanChatUdpReceiver extends Thread{

	public static final String MSG_FROM_ME = "MSG_FROM_ME"; 
	public static final String MSG_FROM_OTHER = "MSG_FROM_OTHER";
	//�洢�����¼
    public static List<MessageInfo> messageInfos = new ArrayList<MessageInfo>();

    public static List<ListContactInfo> listContactInfos = new ArrayList<ListContactInfo>();

    //�洢�������ݿ�
    private MsgSQLiteOpenHelper helper;
    //���ݿ��message����ʵ����
    private MessageDao messageDao;

    private int port;
    //ֹͣ��־
    private boolean flag = true;

    private DatagramSocket da = null;

    private Context context;

    public CanChatUdpReceiver(Context context,int port){
        this.context = context;
        this.port = port;

        //context.deleteDatabase("message.db");//ɾ�����ݿ��ʼ��
        //�������ݿ�
        helper = new MsgSQLiteOpenHelper(context);
        //���ݿ��message����ʵ����
        messageDao =new MessageDao(helper);

        List<MessageInfo> msgInfos = messageDao.findAll();
        if(msgInfos.size() > 50) {//��ȡһ�������ݿ�����
            for (int i = msgInfos.size()-40; i < msgInfos.size(); i++) {
                messageInfos.add(msgInfos.get(i));
            }
        }else{
            for (int i = 0; i < msgInfos.size(); i++) {
                messageInfos.add(msgInfos.get(i));
            }
        }

        //��ʼ���б��е�Ⱥ������
        ListContactInfo listContactInfo = new ListContactInfo();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time =simpleDateFormat.format(calendar.getTime());

        listContactInfo.setListName("������Ⱥ��");
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

                //���ݷ�����ʼ �ж���������

                if(data.contains(Udp.CHECKED_CODE)){
                    //�ж�������������//�ж�������������
                    //ȡ�� �޳��ж��벿�� ȡ���б���Ϣ
                    String listInfoData = data.substring(Udp.CHECKED_CODE.length(),data.length());
                    //���ݽ�����ȡ
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

                    //��ϵ���б����ݼ���
                    if(isAdd) {
                        newListContactInfo.setListIp(ip);
                        listContactInfos.add(newListContactInfo);
                        Log.e("��ӡ��ʾ���ݣ�", "�б�ͳ��������" + listContactInfos.size()+" ip:" + ip);
                    }

                }else{
                    //��Ϣ���ݽ�������
                    MessageInfo messageInfo =getMessageInfo(data);
                    messageInfo.setUserIp(ip);
                    messageInfos.add(messageInfo);
                    //��������Ϣ�㲥
                    Intent intent = new Intent();
                    intent.putExtra("listName",messageInfo.getListName());
                    intent.putExtra("userName",messageInfo.getUserName());
                    intent.putExtra("userIp",messageInfo.getUserIp());
                    intent.putExtra("imageId",messageInfo.getImageId());
                    intent.putExtra("msgBody",messageInfo.getMsgBody());
                    intent.putExtra("receTime",messageInfo.getReceTime());
                    /*
                     * ���յ����Լ��Ĺ㲥��Ϣʱ�����
                     
                    if(messageInfo.getUserIp().equals(MainActivity.localHostIp))
                    	intent.putExtra("flag",MSG_FROM_ME);
                    else
                    	intent.putExtra("flag",MSG_FROM_OTHER);
                    */
                    intent.setAction("com.ADD_VIEW");
                    context.sendBroadcast(intent);

                    //�������ݵ����ݿ�
                   
                    if(!messageInfo.getUserIp().equals(MainActivity.localHostIp))
                    	saveMessageToSQL(messageInfo);
                }

            }
        }catch(Exception e){
            Log.i(e.getMessage(),"����������");
        }finally {
            try{
                if(da !=null)
                    da.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //��Ϣ���ݴ���
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

    //��ϵ���б����ݴ���
    private ListContactInfo getListContactInfo(String data){
        //��������data
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



    //������Ϣ���ݵ����ݿ�
    private void saveMessageToSQL(MessageInfo messageInfo){
        try {
            String userName = messageInfo.getUserName();
            String listName = messageInfo.getListName();
            String userIp = messageInfo.getUserIp();
            int imageId = messageInfo.getImageId();
            String msgBody = messageInfo.getMsgBody();
            String receTime = messageInfo.getReceTime();

            messageDao.add(listName,userName,userIp,imageId,msgBody,receTime);
            Log.e("���ݿ������ʾ��", "����ɹ�");
        }catch (Exception e){
            Log.d(e.getMessage(),"����ʧ��");
        }
    }
}
