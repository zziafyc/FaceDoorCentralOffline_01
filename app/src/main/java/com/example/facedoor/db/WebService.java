package com.example.facedoor.db;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.facedoor.MyApp;

import android.R.integer;
import android.app.Activity;
import android.content.SharedPreferences;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebService {
	private String mIP;
	private String serviceUrl;
	
	public WebService(Activity activity){
		SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
		mIP = config.getString(MyApp.DBIP_KEY, "");
		serviceUrl = "http://" + mIP + ":8080";
	}
	
	public int getNextUserId(){
		int result = -1;
		String url = serviceUrl + "/NextUserId";
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = Integer.parseInt(body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int getNextGroupId() {
		int result = -1;
		String url = serviceUrl + "/NextGroupId";
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = Integer.parseInt(body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean isStaffExist(){
		Boolean result = true;
		String url = serviceUrl + "/IsStaffExist";
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = Boolean.parseBoolean(body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void addGroup(String name){
		String url = serviceUrl + "/AddGroup?Name=" + URLEncoder.encode(name);
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .post(RequestBody.create(null, ""))
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteGroup(int id){
		String url = serviceUrl + "/DeleteGroup?Id=" + id;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .delete()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getGroupId(String name){
		int result = -1;
		String url = serviceUrl + "/GetGroupId?Name=" + URLEncoder.encode(name);
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = Integer.parseInt(body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void addUser(String staffId, String name){
		String url = serviceUrl + "/AddUser?StaffId=" + URLEncoder.encode(staffId) + "&Name=" + URLEncoder.encode(name);
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .post(RequestBody.create(null, ""))
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteUser(int id){
		String url = serviceUrl + "/DeleteUser?Id=" + id;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .delete()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getUserId(String staffId){
		int result = -1;
		String url = serviceUrl + "/GetUserId?StaffId=" + URLEncoder.encode(staffId);
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = Integer.parseInt(body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String getStaffIdAndName(int userId){
		String result = null;
		String url = serviceUrl + "/GetStaffIdAndName?UserId=" + userId;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				result = body;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void addUserGroup(int userId, List<String> groupId){
		String url = serviceUrl + "/AddUserGroup?UserId=" + userId;
		JSONArray jsonArray = new JSONArray(groupId);
		OkHttpClient okHttpClient = new OkHttpClient();
		MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
		RequestBody requestBody = RequestBody.create(mediaType, jsonArray.toString());
		Request request = new Request.Builder()
				   .url(url)
				   .post(requestBody)
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getUserGroup(int userId){
		ArrayList<String> groups = new ArrayList<String>();
		String url = serviceUrl + "/GetUserGroup?UserId=" + userId;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				JSONArray jsonArray = new JSONArray(body);
				for(int i = 0; i < jsonArray.length(); i++){
					groups.add(jsonArray.getString(i));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return groups;
	}
	
	public void deleteUserGroup(int userId){
		String url = serviceUrl + "/DeleteUserGroup?UserId=" + userId;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .delete()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<GroupInfo> getAllGroup(){
		ArrayList<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
		String url = serviceUrl + "/GetAllGroup";
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder()
				   .url(url)
				   .get()
				   .build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				JSONArray jsonArray = new JSONArray(body);
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					GroupInfo groupInfo = new GroupInfo();
					groupInfo.id = jsonObject.getString("id");
					groupInfo.name = jsonObject.getString("name");
					groupInfos.add(groupInfo);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return groupInfos;
	}
}
