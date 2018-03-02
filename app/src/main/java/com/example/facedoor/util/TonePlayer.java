package com.example.facedoor.util;

import java.io.IOException;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.text.TextUtils;

/**
 * 鎻愮ず闊虫挱鏀惧櫒銆�
 * 
 * @author <a href="http://www.xfyun.cn">璁寮�鏀惧钩鍙�</a>
 * @date 2016骞�7鏈�23鏃� 涓婂崍9:53:10 
 *
 */
public class TonePlayer {
	private static MediaPlayer player;

	/**
	 * 鎾斁闊抽璧勬簮銆傛敞锛氬闇�瑕佸彇娑堟挱鏀撅紝鐩存帴璋冪敤杩斿洖鐨凪ediaPlayer鐨剅elease()鏂规硶銆�
	 * 
	 * @param ctx 涓婁笅鏂�
	 * @param resId 璧勬簮id
	 * @param completionRun 鎾斁瀹屾垚鍚庣殑浠诲姟
	 * @return
	 */
	public static MediaPlayer play(Context ctx, int resId, Runnable completionRun) {
		MediaPlayer player = MediaPlayer.create(ctx, resId);
		playInternal(player, completionRun);
		return player;
	}

	/**
	 * 鎾斁闊抽璧勬簮銆傛敞锛氬闇�瑕佸彇娑堟挱鏀撅紝鐩存帴璋冪敤杩斿洖鐨凪ediaPlayer鐨剅elease()鏂规硶銆�
	 * 
	 * @param ctx 涓婁笅鏂�
	 * @param assetsFileName assets璧勬簮鏂囦欢璺緞
	 * @param completionRun 鎾斁瀹屾垚鍚庣殑浠诲姟
	 * @return
	 */
	public static MediaPlayer play(Context ctx, String assetsFileName, Runnable completionRun) {
		MediaPlayer player = createMediaPlayer(ctx, assetsFileName);
		playInternal(player, completionRun);
		return player;
	}

	public static MediaPlayer play(Context ctx, int resId) {
		MediaPlayer player = MediaPlayer.create(ctx, resId);
		playInternal(player, null);
		return player;
	}

	public static MediaPlayer play(Context ctx, String assetsFileName) {
		MediaPlayer player = createMediaPlayer(ctx, assetsFileName);
		playInternal(player, null);
		return player;
	}
	
	private static SoundPool pool;

	public static synchronized void playUseSoundPool(Context context, 
			final String path, final int looptimes) {
		try {
			if (null == context) {
				return;
			}
			
			if (null != pool) {
				pool.release();
			} 
			
			pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			
			final AssetFileDescriptor afd = context.getAssets().openFd(path);
			
			pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					soundPool.play(sampleId, 1, 1, 0, looptimes, 1);
					
					if (null != afd) {
						try {
							afd.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			pool.load(afd, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void playInternal(final MediaPlayer player, final Runnable completionRun) {
		if (player == null) { 
			if (null != completionRun) {
				completionRun.run();
			}
			return;
		}
		
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (completionRun != null) {
					completionRun.run();
				}
				mp.release();
			}
		});
		
		player.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				player.start();
				return true;
			}
		});
		
		player.start();
	}

	private static MediaPlayer createMediaPlayer(Context context, String assetFileName) {
		MediaPlayer mp = new MediaPlayer();
		try {
			if (assetFileName.contains("http")) {
				mp.setDataSource(assetFileName);
				mp.prepare();
				return mp;
			} else {
				AssetFileDescriptor afd = context.getAssets().openFd(assetFileName);
				if (afd == null) {
					return null;
				}
				mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				afd.close();
				mp.prepare();
				return mp;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mp.release();
		}
		return null;
	}
	
	public static void playTone(Context context, String assetFileName) {
		playTone(context, assetFileName, null);
	}

	public static synchronized void playTone(Context context, String assetFileName, final Runnable finishCb) {
		if (null != player) {
			player.release();
		}
		
		player = new MediaPlayer();
		try {
			if (!TextUtils.isEmpty(assetFileName)) {
				AssetFileDescriptor afd = context.getAssets().openFd(assetFileName);
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			
				if (null != afd) {
					afd.close();
				}
				
				player.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						if (null != finishCb) {
							finishCb.run();
						}
						
						mp.release();
						
						player = null;
					}
				});
				
				player.prepare();
				player.start();
			} else {
				player.release();
				player = null;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
	
	public static void stopPlay() {
		synchronized (TonePlayer.class) {
			if (null != player) {
				player.stop();
				player.release();
			}
		}
	}
	
}
