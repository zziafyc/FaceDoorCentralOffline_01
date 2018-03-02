package com.example.facedoor.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;

public class ImageFile {
	
	private static byte[] buffer = new byte[1000];
	/**
	 * 设置保存图片路径
	 * @return
	 */
	private static String getImagePath(String fileName){
		String path;
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return null;
		}
		path =  Environment.getExternalStorageDirectory().getAbsolutePath() +"/FaceVocal/";
		File folder = new File(path);
		if (folder != null && !folder.exists()) {
			folder.mkdirs();
		}
		path += fileName;
		return path;
	}
	
	public static void saveBitmapToFile(Bitmap bitmap, String fileName){
		String file_path = getImagePath(fileName);
		File file = new File(file_path);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readImageFromFile(String fileName){
		String filePath = getImagePath(fileName);
		File file = new File(filePath);
		FileInputStream fIn = null;
		byte[] ImageBytes = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream(15000);
		try {
			fIn = new FileInputStream(file);
			int readSize = 0;
			while((readSize = fIn.read(buffer, 0, buffer.length)) != -1){
				byteOut.write(buffer, 0, readSize);
			}
			byteOut.flush();
			ImageBytes = byteOut.toByteArray();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try {
				if (fIn != null) {
					fIn.close();
				}
				byteOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ImageBytes;
	}
}
