package com.example.testsoundandjni;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testsoundandjni.Recorder.OnStateChangedListener;
import com.htfyun.uartJni.UartController;
import com.htfyun.utils.BoardHelper;
import com.htfyun.utils.DownlinkSwitchState;

public class MainActivity extends Activity implements OnStateChangedListener {

	private static final String TAG = "MainActivity";
	
	/**
	 * 录音状态， false 未录音， true 录音中
	 */
	private boolean isRecording = false;
	/**
	 * 录音
	 */
	Recorder mRecorder = null;
	private int mPlayingRecorderStatus;

	// 背景音乐播放
	private int mPlayingMusicStatus;

	// status.
	private static final int STATUS_IDLE = 0;
	private static final int STATUS_DOING = 1;


	private String[] mUartBaudRateArray = { "1200", "2400", "4800", "9600",
			"14400", "19200", "38400", "56000", "57600", "115200", "128000",
			"256000" };

	
	private Button mBtnDownlinkVolumeAdd;
	private Button mBtnDownlinkVolumeSub;
	private TextView mTxtDownlinkVolume;
	
	private Button mBtnUplinkVolumeAdd;
	private Button mBtnUplinkVolumeSub;
	private TextView mTxtUplinkVolume;
	
	private Button mBtnMediaVolumeAdd;
	private Button mBtnMediaVolumeSub;
	private TextView mTxtMediaVolume;
	
	private TextView mTxtHeadPhoneStatus;
	
	private RadioGroup mRadiogrpDownlinkSwitch;
	private RadioButton mRadiobtnDownlinkAuto;
	private RadioButton mRadiobtnDownlinkHandfree;
	private RadioButton mRadiobtnDownlinkHand;
	
	private Button mBtnRecord;
	private Button mBtnPlayRecord;
	private Button mBtnPlayBgMusic;
	private MediaPlayer mBgMusicPlayer;
	
	private Context mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去掉窗口标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		 mContext = this;
		
		 initNormalVolume();
		 initHeadPhoneStatus();
		 initDownlinkRouteSwitch();
		 initRecord();
		 initDialing();
	}
	
	/**
	 * 常规音量调节初始化
	 */
	public void initNormalVolume() {
		
		 mBtnDownlinkVolumeAdd = (Button) findViewById(R.id.btn_downlink_add);
		 mBtnDownlinkVolumeSub = (Button) findViewById(R.id.btn_downlink_sub);
		 mTxtDownlinkVolume  = (TextView) findViewById(R.id.txt_downlink_volume);
		 
		 mTxtDownlinkVolume.setText("" + BoardHelper.getInstance().getDownlinkVolume(mContext));
		 
		 mBtnUplinkVolumeAdd = (Button) findViewById(R.id.btn_uplink_add);
		 mBtnUplinkVolumeSub = (Button) findViewById(R.id.btn_uplink_sub);
		 mTxtUplinkVolume  = (TextView) findViewById(R.id.txt_uplink_volume);
		 
		 mTxtUplinkVolume.setText("" + BoardHelper.getInstance().getUplinkVolume(mContext));

		 mBtnMediaVolumeAdd = (Button) findViewById(R.id.btn_media_add);
		 mBtnMediaVolumeSub = (Button) findViewById(R.id.btn_media_sub);
		 mTxtMediaVolume  = (TextView) findViewById(R.id.txt_media_volume);
		 
		 mTxtMediaVolume.setText("" + BoardHelper.getInstance().getMediaVolume(mContext));
		 
		 mBtnDownlinkVolumeAdd.setOnClickListener(mVolumeOnClickListener);
		 mBtnDownlinkVolumeSub.setOnClickListener(mVolumeOnClickListener);
		 mBtnUplinkVolumeAdd.setOnClickListener(mVolumeOnClickListener);
		 mBtnUplinkVolumeSub.setOnClickListener(mVolumeOnClickListener);
		 mBtnMediaVolumeAdd.setOnClickListener(mVolumeOnClickListener);
		 mBtnMediaVolumeSub.setOnClickListener(mVolumeOnClickListener);
		
	}

	private void initHeadPhoneStatus() {
		mTxtHeadPhoneStatus =  (TextView) findViewById(R.id.txt_headphone_status);
		BoardHelper.getInstance().setOnHeadPhoneStateListener(mContext, new BoardHelper.OnHeadPhoneStateListener() {

			@Override
			public void onHeadphoneStateListener(boolean isHeadphoneDown) {
				// TODO Auto-generated method stub
				
				mTxtHeadPhoneStatus.setText(isHeadphoneDown ? "放下" : "拿起");
			}
			
		});
	}
	

	private void initDownlinkRouteSwitch() {
		mRadiogrpDownlinkSwitch = (RadioGroup)findViewById(R.id.radiogrp_downlink_switch);
		mRadiobtnDownlinkAuto = (RadioButton) findViewById(R.id.radiobtn_downlink_auto);
		mRadiobtnDownlinkHandfree = (RadioButton) findViewById(R.id.radiobtn_downlink_handfree);
		mRadiobtnDownlinkHand =  (RadioButton) findViewById(R.id.radiobtn_downlink_hand);
		mRadiogrpDownlinkSwitch.setOnCheckedChangeListener(mRadioChangeListener);
		
	}
	
	
	private View.OnClickListener mVolumeOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final int id = v.getId();
			if (id == mBtnDownlinkVolumeAdd.getId()) {
				
				int curDLVolume = BoardHelper.getInstance().getDownlinkVolume(mContext);
				curDLVolume ++ ;
				int maxDLVolume = BoardHelper.getInstance().getDownlinkMaxVolume(mContext);
				if (curDLVolume > maxDLVolume) {
					curDLVolume = maxDLVolume;
				}
				
				Log.e(TAG, "maxDLVolume = " + maxDLVolume + ", DownlinkVolume = " + BoardHelper.getInstance().getDownlinkVolume(mContext));
				
				BoardHelper.getInstance().setDownlinkVolume(mContext, curDLVolume);
				mTxtDownlinkVolume.setText("" + BoardHelper.getInstance().getDownlinkVolume(mContext));
				
			} else if (id == mBtnDownlinkVolumeSub.getId()) {
				
				int curDLVolume = BoardHelper.getInstance().getDownlinkVolume(mContext);
				curDLVolume -- ;
				int minDLVolume = 0;
				if (curDLVolume < minDLVolume) {
					curDLVolume = minDLVolume;
				}
				
				BoardHelper.getInstance().setDownlinkVolume(mContext, curDLVolume);
				mTxtDownlinkVolume.setText("" + BoardHelper.getInstance().getDownlinkVolume(mContext));
				
			} else if (id == mBtnUplinkVolumeAdd.getId()) {
				
				int curULVolume = BoardHelper.getInstance().getUplinkVolume(mContext);
				curULVolume ++ ;
				int maxULVolume = BoardHelper.getInstance().getUplinkMaxVolume(mContext);
				if (curULVolume > maxULVolume) {
					curULVolume = maxULVolume;
				}
				
				BoardHelper.getInstance().setUplinkVolume(mContext, curULVolume);
				mTxtUplinkVolume.setText("" + BoardHelper.getInstance().getUplinkVolume(mContext));
				
			} else if (id == mBtnUplinkVolumeSub.getId()) {
				
				int curULVolume = BoardHelper.getInstance().getUplinkVolume(mContext);
				curULVolume -- ;
				
				int minULVolume = 0;
				if (curULVolume < minULVolume) {
					curULVolume = minULVolume;
				}
				
				BoardHelper.getInstance().setUplinkVolume(mContext, curULVolume);
				mTxtUplinkVolume.setText("" + BoardHelper.getInstance().getUplinkVolume(mContext));
				
				
			} else if (id == mBtnMediaVolumeAdd.getId()) {
				
				int curMDVolume = BoardHelper.getInstance().getMediaVolume(mContext);
				curMDVolume ++ ;
				int maxMDVolume = BoardHelper.getInstance().getMediaMaxVolume(mContext);
				if (curMDVolume > maxMDVolume) {
					curMDVolume = maxMDVolume;
				}
				
				BoardHelper.getInstance().setMediaVolume(mContext, curMDVolume);
				mTxtMediaVolume.setText("" + BoardHelper.getInstance().getMediaVolume(mContext));
				
				
			} else if (id == mBtnMediaVolumeSub.getId()) {
				
				int curMDVolume = BoardHelper.getInstance().getMediaVolume(mContext);
				curMDVolume -- ;
				int minMDVolume = 0;
				if (curMDVolume < minMDVolume) {
					curMDVolume = minMDVolume;
				}
				
				BoardHelper.getInstance().setMediaVolume(mContext, curMDVolume);
				mTxtMediaVolume.setText("" + BoardHelper.getInstance().getMediaVolume(mContext));
				
			}
		}
	};
	
	private RadioGroup.OnCheckedChangeListener mRadioChangeListener = new 
	           RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					// TODO Auto-generated method stub
					final int groupId = group.getId();
					if (groupId == mRadiogrpDownlinkSwitch.getId() ) {
						DownlinkSwitchState state = DownlinkSwitchState.auto;
						if (checkedId == mRadiobtnDownlinkHandfree.getId()) {
							
							state = DownlinkSwitchState.handfree;
							
						} else if (checkedId == mRadiobtnDownlinkHand.getId()) {
							
							state = DownlinkSwitchState.hand;
						} else if (checkedId == mRadiobtnDownlinkAuto.getId()) {
							
						}
						
						boolean isRouteOn = BoardHelper.getInstance().isDownlinkRouteOn(mContext);
						
						BoardHelper.getInstance().setDownlinkSwitchState(state);
						
						if (isRouteOn) {
							BoardHelper.getInstance().setDownlinkRouteOn(mContext, true);
						}
						
						mTxtDownlinkVolume.setText("" + BoardHelper.getInstance().getDownlinkVolume(mContext));
						
					}
				}
		
	};


	/**
	 * 录音初始化
	 */

	private void initRecord() {
		
		mRecorder = new Recorder(); // 录音类初始化
		mRecorder.setOnStateChangedListener(this);
		
		mBtnRecord = (Button) findViewById(R.id.button_record_spk);
		mBtnPlayRecord = (Button) findViewById(R.id.button_play_record);
		mBtnPlayBgMusic = (Button) findViewById(R.id.button_play_bg_audio);
		
		mBtnRecord.setOnClickListener(mRecordOnClickListener);
		mBtnPlayRecord.setOnClickListener(mRecordOnClickListener);
		mBtnPlayBgMusic.setOnClickListener(mRecordOnClickListener);
	}
	
	private View.OnClickListener mRecordOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final int id = v.getId();
			if (id == mBtnRecord.getId()) {
				
				mBtnRecord.setEnabled(false);
				if (isRecording) // 停止录音
				{
					isRecording = false;
					mBtnRecord.setText("开始录音");
					mBtnRecord.setTextColor(Color.BLACK);

					mRecorder.stopRecording();

				} else {
				// 开始录音
					isRecording = true;
					// ModelDevice.getInstance().StartCalling(1);
					mRecorder.startRecording(
							MediaRecorder.OutputFormat.AMR_NB, ".amr",
							mContext);
					mBtnRecord.setText("停止录音");
					mBtnRecord.setTextColor(Color.RED);

				}
				mBtnRecord.setEnabled(true);
				
			} else if (id == mBtnPlayRecord.getId()) {
				
				if (mPlayingRecorderStatus == STATUS_IDLE) {
					
					mRecorder.startPlayback();
					mPlayingRecorderStatus = STATUS_DOING;

					mBtnPlayRecord.setText("停止播放录音");
					mBtnPlayRecord.setTextColor(Color.RED);
					
				} else {
					
					mRecorder.stopPlayback();
					mPlayingRecorderStatus = STATUS_IDLE;
					mBtnPlayRecord.setText("开始播放录音");
					mBtnPlayRecord.setTextColor(Color.BLACK);
				}
				
			} else if (id == mBtnPlayBgMusic.getId()) {
				
				if (mPlayingMusicStatus == STATUS_IDLE) {
					
					startPlayBgMusic(R.raw.test_music);
					mBtnPlayBgMusic.setText("停止播放背景音乐");
					mBtnPlayBgMusic.setTextColor(Color.RED);
					
				} else {
					
					stopPlayBgMusic();
					mBtnPlayBgMusic.setText("开始播放背景音乐");
					mBtnPlayBgMusic.setTextColor(Color.BLACK);
				}
			}
		}
	};
	
	
	private void startPlayBgMusic(int resId) {
		
		stopPlayBgMusic();
		
		 mBgMusicPlayer = MediaPlayer.create(mContext, resId);
		if (mBgMusicPlayer != null) {
			mBgMusicPlayer.start();
			mPlayingMusicStatus = STATUS_DOING;
		}
	}
	
	private void stopPlayBgMusic() {
		
		if (mBgMusicPlayer != null) {
			mBgMusicPlayer.stop();
			mBgMusicPlayer.release();
			mBgMusicPlayer = null;
			mPlayingMusicStatus = STATUS_IDLE;
		}
	}	

	
	private Spinner mSpinnerUartBaudRate;
	private Button mBtnUart;
	private Switch mSwitchHook;
	private Button mBtnDialing;
	
	private EditText mEdittxtDialingNumber;
	
	private View mLayoutDialing;
	
	private TextView mTxtUartReadData;
	
	private final String UART_CHANNEL = "/dev/ttyS1";
	private UartController mUartController;
	/**
	 * 电话拨号初始化
	 */
	private void initDialing() {
		mSpinnerUartBaudRate = (Spinner)findViewById(R.id.spinner_com_brate);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mUartBaudRateArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerUartBaudRate.setAdapter(adapter);
		mSpinnerUartBaudRate.setSelection(9);
		
		mBtnUart = (Button)findViewById(R.id.button_uart);
		mLayoutDialing = findViewById(R.id.view_dialing_layout);
		mSwitchHook = (Switch) findViewById(R.id.switch_hook);
		mBtnDialing = (Button) findViewById(R.id.button_dialing);
		
		mEdittxtDialingNumber = (EditText) findViewById(R.id.etext_dialing_number);
		mTxtUartReadData = (TextView) findViewById(R.id.tview_com_read_data);
		
		mBtnUart.setOnClickListener(mDialingOnClickListener);
		mBtnDialing.setOnClickListener(mDialingOnClickListener);
		
		mSwitchHook.setOnCheckedChangeListener(mHookOnCheckedChangeListener);
		
		showUartOpenText(true);
		
		BoardHelper.getInstance().setDownlinkRouteOn(mContext, false);
	}
	
	private void showUartOpenText(boolean open) {
		mBtnUart.setText("关闭串口");
		int visibility = View.VISIBLE;
		if (open) {
			mBtnUart.setText("打开串口");
			visibility = View.INVISIBLE;
		}
		mLayoutDialing.setVisibility(visibility);
	}
	
	private boolean sendUartCmd(byte[] cmd, int length) {
		if (mUartController == null && !mUartController.isUartOpen()) {
			Toast.makeText(mContext, "请打开串口。",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		int len = mUartController.writeUart(cmd, length);
		return (len == length);
	}
	
	private CompoundButton.OnCheckedChangeListener mHookOnCheckedChangeListener =
			new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					byte[] cmd = "ATH\r\n".getBytes();
					String action = "挂机";
					
					boolean routeOn = false;
					
					if (isChecked) {
						cmd = "ATZ\r\n".getBytes();
						action = "摘机";
						routeOn = true;
					}
					
					BoardHelper.getInstance().setDownlinkRouteOn(mContext, routeOn);
					BoardHelper.getInstance().setUplinkRouteOn(mContext, routeOn);
					
					if (sendUartCmd(cmd, cmd.length)) {
						Toast.makeText(mContext, action + "命令发送成功!",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, action + "命令发送失败!",
								Toast.LENGTH_SHORT).show();
					}
					
				}
			};
	
	private View.OnClickListener mDialingOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final int id = v.getId();
			if (id == mBtnUart.getId()) {
				
				if (mUartController == null) {
					
					String stringBRate = mUartBaudRateArray[mSpinnerUartBaudRate
					           							.getSelectedItemPosition()];
					int brate = -1;
					if (TextUtils.isDigitsOnly(stringBRate)) {
						brate = Integer.valueOf(stringBRate);
					}
					mUartController = new UartController(UART_CHANNEL, brate);
					boolean openOk = mUartController.openUart();
					
					if (!openOk) {
						
						Toast.makeText(mContext, "打开串口失败!",
								Toast.LENGTH_SHORT).show();
						mUartController  = null;
						
					} else {
						
						mUartController.registerUartReadListener(mOnUartReadListener);
						showUartOpenText(false);
					}
					
				} else {
					
					if (mUartController.isUartOpen()) {
						
						mUartController.unregisterUartReadListener(mOnUartReadListener);
						mUartController.closeUartFd();
					}
					
					mUartController = null;
					
					showUartOpenText(true);
				}
				
			} else if (id == mBtnDialing.getId()) {
				
				String tempNumber = mEdittxtDialingNumber.getText().toString().trim();
				
				if (TextUtils.isEmpty(tempNumber)) {
					Toast.makeText(mContext, "号码不能为空。",
							Toast.LENGTH_SHORT).show();
					return;
				}

				String number = "ATD" + tempNumber + "\r\n";
				Log.d(TAG, "拨打电话 : " + number);

				boolean sendOk = sendUartCmd(number.getBytes(), number.length());
				
				String show = tempNumber + " 拨号失败! ";
				if (sendOk) {
					show = tempNumber + " 拨号成功! ";
				}
				Toast.makeText(mContext, show, Toast.LENGTH_SHORT)
						.show();
			}
		}
	};
	
	private final int MSG_READ_UART = 1;
	private Handler mDialingHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			if (msg.what == MSG_READ_UART) {
				mTxtUartReadData.append((String)msg.obj);
				ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
				int offset = mTxtUartReadData.getMeasuredHeight()
						- scrollView.getMeasuredHeight();
				if (offset < 0) {
					offset = 0;
				}
				scrollView.scrollTo(0, offset);
			}
		};
	};
	private UartController.OnUartReadListener mOnUartReadListener = 
			new UartController.OnUartReadListener() {
				
				@Override
				public int onUartReadListener(byte[] data, int length) {
					// TODO Auto-generated method stub
					
					String readData = new String(data, 0, length);
					String hexData = ", hex = ";
					for (int i = 0; i < length; i++) {
						
						hexData += String.format("%02X ", data[i]);
					}
					
					mDialingHandler.obtainMessage(MSG_READ_UART, readData + hexData).sendToTarget();
					
					return 0;
				}
			};

	@Override
	public void onStateChanged(int state) {
		// TODO Auto-generated method stub
		if (state == Recorder.RECORDING_STATE) {
			// text_loginfo.append("\r\nStarting recording..." );
		} else if (state == Recorder.PLAYING_STATE) {
			// text_loginfo.append("\r\nStarting playing recorded file..." );
		} else {
			// IDLE state
			// text_loginfo.append("\r\nRecording/playback finish!");
			if (isRecording) {
				isRecording = false;
				Button button = (Button) findViewById(R.id.button_record_spk);
				button.setText("开始录音");
				button.setTextColor(Color.BLACK);
				/*
				 * button = (Button) findViewById(R.id.button_record_headset);
				 * button.setText("开始手柄录音录音"); button.setTextColor(Color.BLACK);
				 */
			} else if (mPlayingRecorderStatus == STATUS_DOING) {
				mPlayingRecorderStatus = STATUS_IDLE;
				Button buttonPlayRecord = (Button) findViewById(R.id.button_play_record);
				buttonPlayRecord.setText("开始播放录音");
				buttonPlayRecord.setTextColor(Color.BLACK);
			}
		}
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		String message;
		switch (error) {
		case Recorder.SDCARD_ACCESS_ERROR:
			message = "error_sdcard_access";
			break;
		case Recorder.IN_CALL_RECORD_ERROR:
			// TODO: update error message to reflect that the recording could
			// not be
			// performed during a call.
			message = "error_in_calling";
			break;
		case Recorder.INTERNAL_ERROR:
			message = "error_app_internal";
			break;
		default:
			message = "other error.";
		}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

}
