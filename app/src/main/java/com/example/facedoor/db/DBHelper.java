package com.example.facedoor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "test.db";
	private static final int DATABASE_VERSION = 1;
	
	public DBHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(20), group_id CHAR(10))");		
		// we want to use the field "id" as authId(6-18 char) in face identify, make it autoincrement.
		db.execSQL("INSERT INTO user VALUES(10000, 'admin', '0000000000')");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS group_info(group_name VARCHAR(7), group_id CHAR(10) PRIMARY KEY)");
		db.execSQL("INSERT INTO group_info VALUES('invalid', '0000000000')");		

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	
}
