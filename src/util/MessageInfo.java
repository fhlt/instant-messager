package util;

/**
 * userName 昵称
 * userip ip
 * userImageId 头像id
 * receTime 接收时间
 */
public class MessageInfo {

    private String listName;//列表名
    private String userName;//列表名
    private String userIp;//列表名
    private String receTime;//接受或者发送的时间
    private String msgBody;//信息主题
    private int id;//序列号
    private int imageId;
    private String flag;//标记

    public void setImageId(int imageId){
        this.imageId = imageId;
    }
    public int getImageId(){
        return imageId;
    }
    public void setListName(String listName){
        this.listName =listName;
    }
    public String getListName(){
        return listName;
    }
    public void setUserName(String userName){
        this.userName =userName;
    }
    public String getUserName(){
        return userName;
    }
   
    public void setUserIp(String userIp){
        this.userIp =userIp;
    }
    public String getUserIp(){
        return userIp;
    }
    public void setReceTime(String receTime){
        this.receTime =receTime;
    }
    public String getReceTime(){
        return receTime;
    }
    public void setMsgBody(String msgBody){
        this.msgBody = msgBody;
    }
    public String getMsgBody(){
        return msgBody;
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
}
