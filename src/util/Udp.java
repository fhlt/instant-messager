package util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

public class Udp {

	//���嵥���˿�
    public static final int PORT_OWN = 52000;
    //����Ⱥ�Ķ˿�
    public static final int PORT_ALL = 51000;

    public static final String CHECKED_CODE = "check_code_123456789";
    
    //��ȡ�㲥��ַ
    public static String getBroadcast() throws SocketException {  
        System.setProperty("java.net.preferIPv4Stack", "true");  
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {  
            NetworkInterface ni = niEnum.nextElement();  
            if (!ni.isLoopback()) {  
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {  
                    if (interfaceAddress.getBroadcast() != null) {  
                        return interfaceAddress.getBroadcast().toString().substring(1);  
                    }  
                }  
            }  
        }  
        return null;  
    }  
	//����㲥ip
    public static String getIpToAll(){
        try {
            String ip = getIp();
            if(ip == null)
                return  null;
            return getIp().substring(0, 10) + "255";
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //��ȡ����ip
    public static String getIp(){
    	String ipaddress = null;
        try{
        	Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			// �������õ�����ӿ�
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// ����ÿһ���ӿڰ󶨵�����ip
				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ip
									.getHostAddress())) {
						return ipaddress = ip.getHostAddress();
					}
				}
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ipaddress;
    }
}
