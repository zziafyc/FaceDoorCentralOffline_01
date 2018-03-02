package com.example.facedoor.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.facedoor.model.Group;

import java.util.ArrayList;

public class DBManage {
    private DBHelper mHelper;
    private SQLiteDatabase mDataBase;

    public DBManage(Context context) {
        mHelper = new DBHelper(context);
        mDataBase = mHelper.getWritableDatabase();
    }

    public void insertUser(String name, String groupId) {
        mDataBase.execSQL("INSERT INTO user VALUES(null, ?, ?)", new Object[]{name, groupId});
    }

    public void deleteUser(int userId) {
        mDataBase.execSQL("DELETE FROM user WHERE id = ?", new Object[]{userId});
    }

    public int queryUserId(String name) {
        Cursor c = mDataBase.rawQuery("SELECT * FROM user WHERE name = ?", new String[]{name});
        int userId = 0;
        while (c.moveToNext()) {
            userId = c.getInt(c.getColumnIndex("id"));
        }
        c.close();
        return userId;
    }

    public String queryUserName(int userId) {
        Cursor c = mDataBase.rawQuery("SELECT * FROM user WHERE id = ?", new String[]{"" + userId});
        String userName = null;
        while (c.moveToNext()) {
            userName = c.getString(c.getColumnIndex("name"));
        }
        c.close();
        return userName;

    }

    public String queryUserGroupId(String name) {
        Cursor c = mDataBase.rawQuery("SELECT * FROM user WHERE name = ?", new String[]{name});
        String groupId = null;
        while (c.moveToNext()) {
            groupId = c.getString(c.getColumnIndex("group_id"));
        }
        c.close();
        return groupId;

    }

    public ArrayList<String> getUserName() {
        ArrayList<String> names = new ArrayList<String>();
        Cursor c = mDataBase.rawQuery("SELECT * FROM user", null);
        // first row is dumb, skip it
        c.moveToNext();
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex("name"));
            names.add(name);
        }
        c.close();
        return names;
    }

    //获取所有组
    public ArrayList<Group> getGroups() {
        ArrayList<Group> groups = new ArrayList<>();
        Cursor c = mDataBase.rawQuery("SELECT * FROM group_info", null);
        c.moveToNext();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex("group_id"));
            String name = c.getString(c.getColumnIndex("group_name"));
            groups.add(new Group(id, name));
        }
        c.close();
        return groups;
    }

    public ArrayList<String> getGroupName() {
        ArrayList<String> names = new ArrayList<String>();
        Cursor c = mDataBase.rawQuery("SELECT * FROM group_info", null);
        c.moveToNext();
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex("group_name"));
            names.add(name);
        }
        c.close();
        return names;
    }

    public ArrayList<String> getGroupId() {
        ArrayList<String> ids = new ArrayList<String>();
        Cursor c = mDataBase.rawQuery("SELECT * FROM group_info", null);
        c.moveToNext();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex("group_id"));
            ids.add(id);
        }
        c.close();
        return ids;
    }

    public void insertGroup(String groupName, String groupId) {
        mDataBase.execSQL("INSERT INTO group_info VALUES(?, ?)", new Object[]{groupName, groupId});
    }

    public String queryGroupId(String groupName) {
        Cursor c = mDataBase.rawQuery("SELECT * FROM group_info WHERE group_name = ?", new String[]{groupName});
        String groupId = null;
        while (c.moveToNext()) {
            groupId = c.getString(c.getColumnIndex("group_id"));
        }
        c.close();
        return groupId;
    }

    public void deleteGroup(String groupId) {
        mDataBase.execSQL("DELETE FROM group_info WHERE group_id = ?", new String[]{groupId});
    }

    public void closeDB() {
        mDataBase.close();
    }
}
