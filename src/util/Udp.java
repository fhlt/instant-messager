package util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

public class Udp {

	//定义单独端口
    public static final int PORT_OWN = 52000;
    //定义群聊端口
    public static final int PORT_ALL = 51000;

    public static final String CHECKED_CODE = "check_code_123456789";
    
    //获取广播地址
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
	//计算广播ip
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

    //获取本地ip
    public static String getIp(){
    	String ipaddress = null;
        try{
        	Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			// 遍历所用的网络接口
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 遍历每一个接口绑定的所有ip
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
