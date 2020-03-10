package com.mir.urcap.MiRintegration.common;

public class StatusThread extends Thread {
	private final Thread t;
	private final MirXmlRpcAsyncCommunicator mirXmlRpcHandle;
	
	private String status = "";
	private String currentMission = "";
	
	public StatusThread(MirXmlRpcAsyncCommunicator handle) {
		this.mirXmlRpcHandle = handle;
		
		t = new Thread(this, "statusThread");
	}
	
	public void run() {
		try {
			while (true) {
				mirXmlRpcHandle.get_status(new XmlRpcCallback<String>() {
					@Override
					public void getResult(String result) {
						status = result;
					}
				});
				
				mirXmlRpcHandle.get_current_mission(new XmlRpcCallback<String>() {
					@Override
					public void getResult(String result) {
						currentMission = result;
					}
				});
				
				Thread.sleep(1000);
			}
			
		} catch (InterruptedException e) {
			System.out.println("Status thread interrupted: " + e.getMessage());
		}
	}
	
	public void start() {
		if (t != null) {
			t.start();
		}
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getCurrentMission() {
		return currentMission;
		
	}
}
