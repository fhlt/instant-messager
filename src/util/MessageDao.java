package util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MessageDao {
    private MsgSQLiteOpenHelper helper;
    public MessageDao(MsgSQLiteOpenHelper helper){
        this.helper = helper;
    }

    public long add(String listName,String userName,String userIp,int imageId,String messageBody,String receTime){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("listname",listName);
        values.put("username",userName);
        values.put("imageid",imageId);
        values.put("userip",userIp);
        values.put("messagebody",messageBody);
        values.put("recetime",receTime);

        long num = db.insert("message",null,values);
        db.close();
        return num;
    }

    public int delete(String userName){
        SQLiteDatabase db = helper.getWritableDatabase();
        int num = db.delete("message", "username=?", new String[]{userName});
        db.close();
        return  num;
    }

    public int update(String username,String newMesssageBody){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messagebody", newMesssageBody);
        int num = db.update("message", values, "username=?", new String[]{username});
        db.close();
        return num;
    }

    public boolean find(String username){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("message", null, "username=?", new String[]{username}, null, null, null);
        boolean result = cursor.moveToNext();
        cursor.close();
        db.close();
        return result;
    }

    public List<MessageInfo> findAll(){
        SQLiteDatabase db = helper.getReadableDatabase();
        List<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
        Cursor cursor = db.query("message",new String[]{"id","listname","username","userip","imageid","messagebody","recetime"},null,null,null,null,null);
        while(cursor.moveToNext()){

            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String listName = cursor.getString(cursor.getColumnIndex("listname"));
            String userName = cursor.getString(cursor.getColumnIndex("username"));
            String userIp = cursor.getString(cursor.getColumnIndex("userip"));
            int imageId = cursor.getInt(cursor.getColumnIndex("imageid"));
            String messageBody = cursor.getString(cursor.getColumnIndex("messagebody"));
            String receTime = cursor.getString(cursor.getColumnIndex("recetime"));

            MessageInfo messageInfo = new MessageInfo();

            messageInfo.setMsgBody(messageBody);
            messageInfo.setUserName(userName);
            messageInfo.setId(id);
            messageInfo.setListName(listName);
            messageInfo.setReceTime(receTime);
            messageInfo.setUserIp(userIp);
            messageInfo.setImageId(imageId);
            
            messageInfos.add(messageInfo);
        }
        cursor.close();
        db.close();
        return messageInfos;
    }
}

