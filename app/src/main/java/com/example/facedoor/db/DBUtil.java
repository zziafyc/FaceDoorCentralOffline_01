package com.example.facedoor.db;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.facedoor.MyApp;
import com.example.facedoor.model.Group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    static {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String mIP;
    private String mUser;
    private String mPWD;
    private String mDB;

    public DBUtil(String ip, String user, String pwd, String db) {
        init(ip, user, pwd, db);
    }

    public DBUtil(String ip) {
        this(ip, "WebUser", "Webuser@123", "FaceVocal");
    }

    public DBUtil(Activity activity) {
        SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
        String dbIP = config.getString(MyApp.DBIP_KEY, "");
        // init(dbIP, "sa", "root2012", "Test");
        init(dbIP, "WebUser", "Webuser@123", "FaceVocal");
    }

    private void init(String ip, String user, String pwd, String db) {
        mIP = ip;
        mUser = user;
        mPWD = pwd;
        mDB = db;
    }

    private Connection getConnection() {
        DriverManager.setLoginTimeout(3);
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://" + mIP + ":1433/" + mDB, mUser, mPWD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public boolean testConnection() {
        Connection con = getConnection();
        return con == null ? false : true;
    }

    public String queryGroup(String groupId) throws Exception {
        String result = null;
        String sql = "SELECT * FROM groups WHERE id = " + groupId;
        Connection con = getConnection();
        if (con == null) {
            throw new Exception("Get jdbc connection failed");
        }

        Statement query = null;
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(sql);
            while (rs.next()) {
                if (rs.getString("name") != null) {
                    result = rs.getString("name");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public int getNextUserId() {
        int result = -1;
        String sql = "select IDENT_CURRENT('users')";
        Connection con = getConnection();
        if (con == null) {
            return result;
        }
        Statement query = null;
        try {
            boolean emptyTable = false;
//			ResultSet topRS= query.executeQuery("select top 1 * from groups");
//			if(topRS.next()){
//				emptyTable = false;
//			}
            query = con.createStatement();
            ResultSet rs = query.executeQuery(sql);
            if (rs.next()) {
                int currentIdent = rs.getInt(1);
                result = emptyTable ? currentIdent : currentIdent + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public int getNextGroupId() {
        int result = -1;
        String sql = "select IDENT_CURRENT('groups')";
        Connection con = getConnection();
        if (con == null) {
            return result;
        }
        Statement query = null;
        try {
            boolean emptyTable = false;
            query = con.createStatement();
//			ResultSet topRS= query.executeQuery("select top 1 * from groups");
//			if(topRS.next()){
//				emptyTable = false;
//			}
            ResultSet rs = query.executeQuery(sql);
            if (rs.next()) {
                int currentIdent = rs.getInt(1);
                result = emptyTable ? currentIdent : currentIdent + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public ArrayList[] queryLocalUsers(String groupId) throws Exception {
        String sql = "select staff_id,name from user_group as ug join users as u on ug.id=u.id and ug.group_id="
                + groupId;
        Connection con = getConnection();
        if (con == null) {
            throw new Exception("Get jdbc connection failed");
        }

        ArrayList staffID = new ArrayList();
        ArrayList name = new ArrayList();
        ArrayList[] results = {staffID, name};
        Statement query = null;
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(sql);
            while (rs.next()) {
                staffID.add(rs.getString("staff_id"));
                name.add(rs.getString("name"));
                ;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return results;
    }

    public boolean isStaffExist(String staffID) throws Exception {
        boolean result = true;
        Connection con = getConnection();
        if (con == null) {
            throw new Exception("Get jdbc connection failed");
        }

        Statement query = null;
        String queryStr = "SELECT name FROM users WHERE staff_id = '" + staffID + "'";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            if (!rs.next()) {
                result = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public void insertHistory(String staffID, String doorID, File image) {
        FileInputStream fileInputStream = null;
        Connection con = getConnection();
        if (con == null) {
            return;
        }
        PreparedStatement insert = null;
        String sql = "insert into history(staff_id, door_id, face) values(?, ?, ?)";
        try {
            fileInputStream = new FileInputStream(image);
            insert = con.prepareStatement(sql);
            insert.setString(1, staffID);
            insert.setString(2, doorID);
            insert.setBinaryStream(3, fileInputStream);
            insert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(insert, con);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //fyc查询所有组
    public List<Group> queryAllGroups() {
        List<Group> groups = new ArrayList<>();
        Connection con = getConnection();
        if (con == null) {
            return null;
        }
        Statement query = null;
        String queryStr = "SELECT * FROM groups";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                groups.add(new Group(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return groups;
    }
    public int queryFaceVocal() {
        int result = -1;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        // not support preparedStatement
        Statement query = null;
        String queryStr = "SELECT switch FROM face_vocal";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            if (rs.next()) {
                result = rs.getInt("switch");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;

    }

    public int addGroup(String name) {
        int result = -1;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement insert = null;
        String insertStr = "INSERT INTO groups VALUES(?)";
        try {
            insert = con.prepareStatement(insertStr);
            insert.setString(1, name);
            result = insert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(insert, con);
        }
        return result;
    }

    public int getGroupId(String groupName) {
        int result = -1;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }
        // not support preparedStatement
        Statement query = null;
        String queryStr = "SELECT id FROM groups WHERE name = '" + groupName + "'";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                result = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }
/*	public boolean insertGroup(String id, String name) {
        boolean result = false;
		Connection con = getConnection();
		if (con == null) {
			return result;
		}

		PreparedStatement insert = null;
		String insertStr = "INSERT INTO groups VALUES(?,?)";
		try {
			insert = con.prepareStatement(insertStr);
			insert.setString(1, id);
			insert.setString(2, name);
			insert.executeUpdate();
			result = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(insert, con);
		}
		return result;
	}*/

    public boolean deleteGroup(int id) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement delete = null;
        String deleteStr = "DELETE FROM groups WHERE id = ?";
        try {
            delete = con.prepareStatement(deleteStr);
            delete.setInt(1, id);
            delete.executeUpdate();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(delete, con);
        }
        return result;
    }

    public boolean insertUser(String staffID, String name) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement insert = null;
        String insertStr = "INSERT INTO users(staff_id,name) VALUES(?,?)";
        try {
            insert = con.prepareStatement(insertStr);
            insert.setString(1, staffID);
            insert.setString(2, name);
            insert.executeUpdate();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(insert, con);
        }
        return result;
    }

    public boolean deleteUser(int id) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement delete = null;
        String deleteStr = "DELETE FROM users WHERE id = ?";
        try {
            delete = con.prepareStatement(deleteStr);
            delete.setInt(1, id);
            delete.executeUpdate();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(delete, con);
        }
        return result;
    }

    public int queryUserID(String staffID) {
        int result = -1;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        // not support preparedStatement
        Statement query = null;
        String queryStr = "SELECT id FROM users WHERE staff_id = '" + staffID + "'";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                result = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public String queryStaffIdAndName(int userID) {
        String result = null;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        Statement query = null;
        String queryStr = "SELECT staff_id,name FROM users WHERE id =" + userID;
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                result = rs.getString("staff_id") + ";" + rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public boolean insertUserGroup(int userID, List<String> groupID) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement insert = null;
        String insertStr = "INSERT INTO user_group VALUES(?,?)";
        try {
            con.setAutoCommit(false);
            insert = con.prepareStatement(insertStr);
            for (String gID : groupID) {
                insert.setInt(1, userID);
                insert.setString(2, gID);
                insert.executeUpdate();
            }
            con.commit();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close(insert, con);
        }

        return result;
    }

    public ArrayList<String> queryUserGroups(int authID) {
        ArrayList<String> result = new ArrayList<String>();
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        Statement query = null;
        String queryStr = "SELECT group_id FROM user_group WHERE id = " + authID;
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                result.add(rs.getString("group_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    public boolean deleteUserGroup(int authID) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        PreparedStatement delete = null;
        String deleteStr = "DELETE FROM user_group WHERE id = ?";
        try {
            delete = con.prepareStatement(deleteStr);
            delete.setInt(1, authID);
            delete.executeUpdate();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(delete, con);
        }
        return result;
    }

    public boolean queryGroups(ArrayList<String> id, ArrayList<String> name) {
        boolean result = false;
        Connection con = getConnection();
        if (con == null) {
            return result;
        }

        Statement query = null;
        String queryStr = "SELECT * FROM groups";
        try {
            query = con.createStatement();
            ResultSet rs = query.executeQuery(queryStr);
            while (rs.next()) {
                int groupId = rs.getInt("id");
                id.add("" + groupId);
                name.add(rs.getString("name"));
            }
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(query, con);
        }
        return result;
    }

    private static void close(Statement state, Connection con) {
        if (state != null) {
            try {
                state.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
