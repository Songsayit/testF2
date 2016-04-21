package com.htfyun.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.text.TextUtils;

public class BoardHelper {

	private static final String RESERVD_GPIO_STATE_PATH = "/sys/class/switch/rvgpio/state";
	private static final String BROADCAST_ACTION_GPIO_STATE = "android.intent.action.RESERVED_GPIO";
	private static final int GPIO_STATE_HEADPHONE_BIT = 1 << 0; //听筒状态的管脚监听
	
	private final static int MODE_STREAM_DOWNLINK_HANDFREE = 200;//免提
	private final static int MODE_STREAM_DOWNLINK_HAND = 201; //手持
    private final static int MODE_STREAM_UPLINK = 202;
    
    private static BoardHelper instance;
    
    private DownlinkSwitchState mDownlinkSwitchState = DownlinkSwitchState.auto;
	
    public interface OnHeadPhoneStateListener {
    	void onHeadphoneStateListener(boolean isHeadphoneDown);
    }
    
    private OnHeadPhoneStateListener mOnHeadPhoneStateListener;
    
    private BoardHelper(){}
    
    public static BoardHelper getInstance() {
    	if (instance == null) {
    		instance = new BoardHelper();
    	}
    	
    	return instance;
    }
    
    public void setOnHeadPhoneStateListener(Context context, OnHeadPhoneStateListener listener) {
    	
    	mOnHeadPhoneStateListener = listener;
    	
    	IntentFilter filter = new IntentFilter(BROADCAST_ACTION_GPIO_STATE);
    	
    	if (mOnHeadPhoneStateListener != null) {
    		
    		context.registerReceiver(mHeadphoneStateReceiver, filter);
    		
    	} else {
    		context.unregisterReceiver(mHeadphoneStateReceiver);
    	}
    	
    }
    
    //downlink
    public void setDownlinkSwitchState(DownlinkSwitchState state) {
    	mDownlinkSwitchState = state;
    }
    
    public DownlinkSwitchState getDownlinkSwitchState() {
    	return mDownlinkSwitchState;
    }

    public void setDownlinkRouteOn(Context context, boolean on) {
    	
		switch (mDownlinkSwitchState) {
		case auto:
			boolean headphoneDown = isHeadphoneDown();
        	if (headphoneDown) {
        		setDownlinkHandfreeRouteOn(context, on);
        	} else {
        		setDownlinkHandRouteOn(context, on);
        	}
			break;
		case handfree:
			setDownlinkHandfreeRouteOn(context, on);
			break;
			
		case hand:
			setDownlinkHandRouteOn(context, on);
			break;

		default:
			break;
		}
    	
    }
    
    
    public void setDownlinkHandfreeRouteOn(Context context, boolean on) {
    	setModeStreamRouteOn(context, MODE_STREAM_DOWNLINK_HANDFREE, on);
    }
    
    public void setDownlinkHandRouteOn(Context context, boolean on) {
    	setModeStreamRouteOn(context, MODE_STREAM_DOWNLINK_HAND, on);
    }
    
    public boolean isDownlinkRouteOn(Context context) {
		boolean on = false;
		switch (mDownlinkSwitchState) {
		case auto:
			boolean headphoneDown = isHeadphoneDown();
        	if (headphoneDown) {
        		on = isDownlinkHandfreeRouteOn(context);
        	} else {
        		on = isDownlinkHandRouteOn(context);
        	}
			break;
		case handfree:
			on = isDownlinkHandfreeRouteOn(context);
			break;
			
		case hand:
			on = isDownlinkHandRouteOn(context);
			break;

		default:
			break;
		}
		
		return on;
    }
    
    public boolean isDownlinkHandfreeRouteOn(Context context) {
    	return getModeStreamRoute(context, MODE_STREAM_DOWNLINK_HANDFREE);
    }
    
    public boolean isDownlinkHandRouteOn(Context context) {
    	return getModeStreamRoute(context, MODE_STREAM_DOWNLINK_HAND);
    }
  
    
    public int getDownlinkVolume(Context context) {
    	
    	int volume = 0; 
    	
    	switch (mDownlinkSwitchState) {
		case auto:
			boolean headphoneDown = isHeadphoneDown();
        	if (headphoneDown) {
        		volume = getDownlinkHandFreeVolume(context);
        	} else {
        		volume = getDownlinkHandVolume(context);
        	}
			break;
		case handfree:
			volume = getDownlinkHandFreeVolume(context);
			break;
			
		case hand:
			volume = getDownlinkHandVolume(context);
			break;

		default:
			break;
		}
    	
    	return volume;
    	
    }
    
    public int getDownlinkMaxVolume(Context context) {
    	int volume = 0;
    	
    	switch (mDownlinkSwitchState) {
		case auto:
			boolean headphoneDown = isHeadphoneDown();
        	if (headphoneDown) {
        		volume = getDownlinkHandFreeMaxVolume(context);
        	} else {
        		volume = getDownlinkHandMaxVolume(context);
        	}
			break;
		case handfree:
			volume = getDownlinkHandFreeMaxVolume(context);
			break;
			
		case hand:
			volume = getDownlinkHandMaxVolume(context);
			break;
	
		default:
			break;
		}
    	
    	return volume;
    }
    
    public void setDownlinkVolume(Context context, int vol) {
    	
    	switch (mDownlinkSwitchState) {
		case auto:
			boolean headphoneDown = isHeadphoneDown();
        	if (headphoneDown) {
        		setDownlinkHandFreeVolume(context, vol);
        	} else {
        		setDownlinkHandVolume(context, vol);
        	}
			break;
		case handfree:
			setDownlinkHandFreeVolume(context, vol);
			break;
			
		case hand:
			setDownlinkHandVolume(context, vol);
			break;
	
		default:
			break;
		}
    }
    
    public void setDownlinkHandFreeVolume(Context context, int vol) {
    	setModeStreamVolume(context, MODE_STREAM_DOWNLINK_HANDFREE, vol );
    }
    
    public void setDownlinkHandVolume(Context context, int vol) {
    	setModeStreamVolume(context, MODE_STREAM_DOWNLINK_HAND, vol );
    }
    
    public int getDownlinkHandFreeVolume(Context context) {
    	return getModeStreamVolume(context, MODE_STREAM_DOWNLINK_HANDFREE);
    }
    
    public int getDownlinkHandFreeMaxVolume(Context context) {
    	return getModeStreamMaxVolume(context, MODE_STREAM_DOWNLINK_HANDFREE);
    }
    
    public int getDownlinkHandVolume(Context context) {
    	return getModeStreamVolume(context, MODE_STREAM_DOWNLINK_HAND);
    }
    
    public int getDownlinkHandMaxVolume(Context context) {
    	return getModeStreamMaxVolume(context, MODE_STREAM_DOWNLINK_HAND);
    }
    
    
    //uplink
    public void setUplinkRouteOn(Context context, boolean on) {
    	setModeStreamRouteOn(context, MODE_STREAM_UPLINK, on);
    }
    
    public boolean isUplinkRouteOn(Context context) {
    	return getModeStreamRoute(context, MODE_STREAM_UPLINK);
    }
    
    public int getUplinkVolume(Context context) {
    	return getModeStreamVolume(context, MODE_STREAM_UPLINK);
    }
    
    public int getUplinkMaxVolume(Context context) {
    	return getModeStreamMaxVolume(context, MODE_STREAM_UPLINK);
    }
    
    public void setUplinkVolume(Context context, int vol) {
    	setModeStreamVolume(context, MODE_STREAM_UPLINK, vol);
    }
    
    //media
    
    public int getMediaVolume(Context context) {
    	return getModeStreamVolume(context, AudioManager.STREAM_MUSIC);
    }
    
    public int getMediaMaxVolume(Context context) {
    	return getModeStreamVolume(context, AudioManager.STREAM_MUSIC);
    }
    
    public void setMediaVolume(Context context, int vol) {
    	setModeStreamVolume(context, AudioManager.STREAM_MUSIC, vol);
    }
    
    /**
     * 听筒是否放下?
     * 放下时是免提, 拿起是手持
     * @return
     * true: handfree, false: hand
     */
    public boolean isHeadphoneDown() {
    	boolean down = false;
    	try {
			String stateStr = readFileALine(RESERVD_GPIO_STATE_PATH);
			if (!(TextUtils.isEmpty(stateStr)) && TextUtils.isDigitsOnly(stateStr)) {
				
				int state = Integer.valueOf(stateStr);
				if ((state & GPIO_STATE_HEADPHONE_BIT) != 0) {
					down = true;
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return down;
    }
    
    private void setModeStreamRouteOn(Context context, int modeStream, boolean on) {
    	AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	if (am == null) {
    		return;
    	}
    	
    	am.setRouting(modeStream, on ? 1 : 0, 0);
    }
    
    private boolean getModeStreamRoute(Context context, int modeStream) {
    	AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	if (am == null) {
    		return false;
    	}
    	
    	return am.getRouting(modeStream) > 0 ? true : false;
    }
    
    private int getModeStreamMaxVolume(Context context, int modeStream) {
    	AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	if (am == null) {
    		return -1;
    	}
    	
    	return am.getStreamMaxVolume(modeStream);
    }
    
    private int getModeStreamVolume(Context context, int modeStream) {
    	AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	if (am == null) {
    		return -1;
    	}
    	
    	return am.getStreamVolume(modeStream);
    }
    
    private void setModeStreamVolume(Context context, int modeStream, int index) {
    	AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	if (am == null) {
    		return;
    	}
    	am.setStreamVolume(modeStream, index, 0);
    }
    
    
    private String readFileALine(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        br.close();
        fr.close();
        
        return line;
    }
    
    private BroadcastReceiver mHeadphoneStateReceiver = 
    		new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					final String action = intent.getAction();
					if (action.equals(BROADCAST_ACTION_GPIO_STATE)) {
						int state = intent.getIntExtra("state", 0);
						boolean isHeadphoneDown = ((state & GPIO_STATE_HEADPHONE_BIT) != 0);
						if (mOnHeadPhoneStateListener != null) {
							mOnHeadPhoneStateListener.onHeadphoneStateListener(isHeadphoneDown);
						}
					}
				}
			};
    
}
