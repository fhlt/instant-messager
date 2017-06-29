package util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * Udp��������
 */
public class CanChatUdpSend implements Runnable {
    //���͵������ı�
    private String data;

    private int port;

    private String ip;

    public CanChatUdpSend(String data,String ip,int port){
        this.data = data;
        this.port = port;
        this.ip = ip;
    }
    public void run() {
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket();
            byte[] buf = data.getBytes();
            DatagramPacket dp = new DatagramPacket(buf,buf.length, InetAddress.getByName(ip),port);
            ds.send(dp);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(ds != null)
                    ds.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

