package util;

/**
 * userName �ǳ�
 * userip ip
 * userImageId ͷ��id
 * receTime ����ʱ��
 */
public class MessageInfo {

    private String listName;//�б���
    private String userName;//�б���
    private String userIp;//�б���
    private String receTime;//���ܻ��߷��͵�ʱ��
    private String msgBody;//��Ϣ����
    private int id;//���к�
    private int imageId;
    private String flag;//���

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
