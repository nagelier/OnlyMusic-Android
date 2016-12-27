package com.fatwest.singlemusicplayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final String mp3path = "file:///android_asset/bigbang.mp3";
	private final String mp3Name = "bangbangbang.mp3";
	private final int replayDelayMillis = 2000;
	MediaPlayer mMediaPlayer = null;
	Button playBtn, pauseBtn;
	private TextView contentTv;
	private boolean isChanging=false;
	private SeekBar mSeekBar;
	private Timer mTimer = null;
	private TimerTask mTimerTask; 
	private Handler mTimeDisPlayHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		contentTv = (TextView) findViewById(R.id.content_tv);
		playBtn = (Button) findViewById(R.id.play_button);
		pauseBtn = (Button) findViewById(R.id.pause_button);
		mSeekBar = (SeekBar) findViewById(R.id.music_sbr);
		mSeekBar.setOnSeekBarChangeListener(new MySeekbar());
		mTimeDisPlayHandler = new Handler(); 
		
		initBaseView();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	private void initBaseView() {
		playBtn = (Button) findViewById(R.id.play_button);
		pauseBtn = (Button) findViewById(R.id.pause_button);
		preparMedia();
		
		playBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				playMedia();
			}
		});

		pauseBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				pauseMedia();
			}
		});

		findViewById(R.id.replay_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						replayMedia();
					}
				});

	}
	
	
	private void preparMedia() {
		
		AssetManager am = this.getAssets();
		try {
//			mMediaPlayer = new MediaPlayer();
//			AssetFileDescriptor afd = am.openFd(mp3Name);
//			mMediaPlayer.setDataSource(afd.getFileDescriptor());
			
			mMediaPlayer = MediaPlayer.create(this, R.raw.bangbangbang);
			mMediaPlayer.prepare(); 
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void replayMedia() {
		if (mMediaPlayer == null) {
			return;
		}
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.seekTo(0);
			mMediaPlayer.pause();
		}
		new Handler().postDelayed(new Runnable() {

			public void run() {
				mMediaPlayer.reset();
				preparMedia();
				mMediaPlayer.start();
				setSeekBar();
			}

		}, replayDelayMillis);
	}
	
	private void setSeekBar() {

		mSeekBar.setMax(mMediaPlayer.getDuration());
		if (mTimerTask == null && mTimer == null) {
			mTimer = new Timer();
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					if (isChanging == true) {
						return;
					}
					mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
					new Thread() {
						public void run() {
							mTimeDisPlayHandler.post(runnableUi);
						}
					}.start();
				}
			};
			mTimer.schedule(mTimerTask, 0, 300);
		}
	}

	private void displayContentTv(){
		int progress = mMediaPlayer.getCurrentPosition();
		contentTv.setText(getTimeStr(progress) + "/"+ getTimeStr(mMediaPlayer.getDuration()));
	}
	
	
	private String getTimeStr(int progress){
		int minutes = progress/1000/60;
		int second = minutes!= 0 ? (progress/1000) - 60*minutes: progress/1000;
		
		String minuteString = minutes > 0 ?  "0"+ minutes : "00";
		
		String prex = second < 10 ? "0" : "";
		String secondString = second > 0 ? prex+ second : "00";
		
		return minuteString + ":" + secondString;
	}
	
	Runnable runnableUi = new Runnable() {
		@Override
		public void run() {
			displayContentTv();
		}
	};
	
	class MySeekbar implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			contentTv.setText(getTimeStr(progress) + "/"+ getTimeStr(mMediaPlayer.getDuration()));
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			isChanging = true;
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			mMediaPlayer.seekTo(mSeekBar.getProgress());
			isChanging = false;
		}

	}
    
    class MediaPreparedListener implements OnPreparedListener{
		@Override
		public void onPrepared(MediaPlayer arg0) {
			mMediaPlayer.start();
		}
    	
    }
	
    private void playMedia(){
    	
    	if (mMediaPlayer != null) {
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				setSeekBar();
			}
		}
    }
    
    private void pauseMedia(){
    	
    	if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
			}
		}
    }
    
	protected void onDestroy() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mTimerTask.cancel();
			mTimer.cancel();
		}
		super.onDestroy();
	}

	protected void onPause() {
		pauseMedia();
		super.onPause();
	}

	protected void onResume() {
		if (mMediaPlayer != null) {
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				setSeekBar();
			}
		}
		super.onResume();
	}
}
