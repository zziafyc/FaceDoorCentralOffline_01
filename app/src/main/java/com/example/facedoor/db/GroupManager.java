package com.example.facedoor.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.facedoor.MyApp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class GroupManager {
	private static final String APPID = "pc20onli";
	private static final String FEATURE_TYPE ="face";
	private static final String DEL_TYPE = "1";
	
//	private static final String SERVICE_URL = "http://192.168.1.248:8080/idauth-web";
//	private static final String JOIN_URL = SERVICE_URL + "/userGroup/addUserToGroup";
//	private static final String QUIT_URL = SERVICE_URL + "/userGroup/delUserFromGroup";
//	private static final String CREATE_URL = SERVICE_URL + "/group/add";
//	private static final String DELETE_URL = SERVICE_URL + "/group/delete?";

	private static final String JOIN_URL = "/userGroup/addUserToGroup";
	private static final String QUIT_URL = "/userGroup/delUserFromGroup";
	private static final String CREATE_URL = "/group/add";
	private static final String DELETE_URL = "/group/delete?";

	private String personnalIP;
	private String serviceUrl;
	
	public GroupManager(Activity activity){
		SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
		personnalIP = config.getString(MyApp.PERSONNAL, "");
		serviceUrl = "http://" + personnalIP + ":8080/idauth-web";
	}
	public  List<String> joinGroup(String userId, List<String> groupId){
		String joinUrl = serviceUrl + JOIN_URL;
		ArrayList<String> success = new ArrayList<String>();
		try {
			JSONObject jsonUser = new JSONObject();
			jsonUser.put("appid", APPID);
			jsonUser.put("userid", userId);
			JSONArray jsonUsers = new JSONArray();
			jsonUsers.put(jsonUser);
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("users", jsonUsers);
			
			OkHttpClient okHttpClient = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

			for(String gId : groupId){
				jsonBody.put("groupId", gId);
				RequestBody requestBody = RequestBody.create(mediaType, jsonBody.toString());
				Request request = new Request.Builder()
						   .url(joinUrl)
						   .post(requestBody)
						   .build();
				Response response = okHttpClient.newCall(request).execute();
				if(response.isSuccessful()){
					byte[] bytes = response.body().bytes();
					String body = new String(bytes, "utf-8");
					JSONObject jsonResponse = new JSONObject(body);
					String responseCode = jsonResponse.getString("responseCode");
					if(responseCode.equals("0000")){
						success.add(gId);
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	public  List<String> quitGroup(String userId, List<String> groupId){
		String quitUrl = serviceUrl + QUIT_URL;
		ArrayList<String> fail = new ArrayList<String>();
		try {
			JSONObject jsonUser = new JSONObject();
			jsonUser.put("appid", APPID);
			jsonUser.put("userid", userId);
			JSONArray jsonUsers = new JSONArray();
			jsonUsers.put(jsonUser);
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("delType", DEL_TYPE);
			jsonBody.put("users", jsonUsers);
			
			OkHttpClient okHttpClient = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

			for(String gId : groupId){
				jsonBody.put("groupId", gId);
				RequestBody requestBody = RequestBody.create(mediaType, jsonBody.toString());
				Request request = new Request.Builder()
						   .url(quitUrl)
						   .post(requestBody)
						   .build();
				Response response = okHttpClient.newCall(request).execute();
				if(response.isSuccessful()){
					byte[] bytes = response.body().bytes();
					String body = new String(bytes, "utf-8");
					Log.e("GroupManager", "quit group:" + body);
					JSONObject jsonResponse = new JSONObject(body);
					String responseCode = jsonResponse.getString("responseCode");
					int successNum = jsonResponse.getInt("successNum");
					if(!responseCode.equals("0000") || successNum != 1){
						fail.add(gId);
					}
				}else{
					fail.add(gId);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fail;
	}
	
	public  String createGroup(String groupId, String groupName){
		String createUrl = serviceUrl + CREATE_URL;
		String responseCode = "";
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody.put("groupId", groupId);
			jsonBody.put("groupName", groupName);
			jsonBody.put("tag", "");
			jsonBody.put("featureType", FEATURE_TYPE);
			
			OkHttpClient okHttpClient = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

			RequestBody requestBody = RequestBody.create(mediaType, jsonBody.toString());
			Request request = new Request.Builder()
					   .url(createUrl)
					   .post(requestBody)
					   .build();
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()){
				byte[] bytes = response.body().bytes();
				String body = new String(bytes, "utf-8");
				JSONObject jsonResponse = new JSONObject(body);
				responseCode = jsonResponse.getString("responseCode");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseCode;
	}
	
	public  String deleteGroup(String groupId){
		String deleteUrl = serviceUrl + DELETE_URL;
		String responseCode = "";
		String url = deleteUrl + "groupId=" + groupId + "&delType=" + DEL_TYPE;
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
				JSONObject jsonResponse = new JSONObject(body);
				responseCode = jsonResponse.getString("responseCode");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseCode;
	}
}
