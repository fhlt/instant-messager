package util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MsgSQLiteOpenHelper extends SQLiteOpenHelper{

	public MsgSQLiteOpenHelper(Context context) {
        super(context,"message.db",null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table message(id integer primary key autoincrement,listname varchar(20),username varchar(20),userip varchar(20),imageid integer,messagebody varchar(20),recetime varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }
}
