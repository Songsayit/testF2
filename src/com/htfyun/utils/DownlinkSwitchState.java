package com.htfyun.utils;

public enum DownlinkSwitchState {
	
	auto(0),
	handfree(1),
	hand(2),
	
	;
	private final int state;
	private DownlinkSwitchState(int state) {
		this.state = state;
	}
}
