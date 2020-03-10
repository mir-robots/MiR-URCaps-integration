package com.mir.urcap.MiRintegration.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class MirXmlRpcAsyncCommunicator {
	
	private final XmlRpcClient client;
	
	public MirXmlRpcAsyncCommunicator(String host, int port) {
		this.client = createXmlRpcClient(host, port);
	}
	
	public MirXmlRpcAsyncCommunicator() {
		this.client = createXmlRpcClient("127.0.0.1", 34567);
	}
	
	private XmlRpcClient createXmlRpcClient(String host, int port) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setEnabledForExtensions(true);
		try {
			String serverURL = new String("http://" + host + ":" + port + "/RPC2");
			config.setServerURL(new URL(serverURL));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		config.setConnectionTimeout(1000);
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		return client;
	}
	
	public void set_ip(String ip) {
		ArrayList<String> args = new ArrayList<String>();
		args.add(ip);
		try {
			client.executeAsync("set_ip", args, new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in set_ip XmlRpc async call: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpx exception: " + e.getMessage());
		}
	}
	
	public void get_register(int register, final XmlRpcCallback<String> callback) {
		ArrayList<Integer> args = new ArrayList<Integer>();
		args.add(register);
		try {
			client.executeAsync("get_register", args, new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
					callback.getResult(arg1.toString()); 
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in getRegister XmlRpc async call: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void write_register(int register, Number value) {
		ArrayList<Number> args = new ArrayList<Number>();
		args.add(register);
		if (register >= 0 && register <= 100) {
			args.add(value.intValue());
		} else {
			args.add(value.doubleValue());
		}
		try {
			client.executeAsync("write_register", args, new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {					
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in write_register function: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void get_register_labels(final XmlRpcCallback<String> callback) {
		try {
			client.executeAsync("get_register_labels", new ArrayList<String>(), new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
					callback.getResult((String) arg1);
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in get_register_labels function: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void get_status(final XmlRpcCallback<String> callback) {
		try {
			client.executeAsync("get_status", new ArrayList<String>(), new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
					callback.getResult((String) arg1);
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in get_status function: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void get_current_mission(final XmlRpcCallback<String> callback) {
		try {
			client.executeAsync("get_current_mission", new ArrayList<String>(), new AsyncCallback() {
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
					callback.getResult((String) arg1); 
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in get_current_mission function: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void continue_robot() {
		try {
			client.executeAsync("continue_robot", new ArrayList<String>(), new AsyncCallback() {
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {					
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in continue_robot: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void pause_robot() {
		try {
			client.executeAsync("pause_robot", new ArrayList<String>(), new AsyncCallback() {
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {					
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in pause_robot: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
	
	public void ping() {
		try {
			client.executeAsync("ping", new ArrayList<String>(), new AsyncCallback() {
				
				@Override
				public void handleResult(XmlRpcRequest arg0, Object arg1) {
					System.out.println((String) arg1);
				}
				
				@Override
				public void handleError(XmlRpcRequest arg0, Throwable arg1) {
					System.out.println("Error in ping XmlRpc async call: " + arg1.getMessage());
				}
			});
		} catch (XmlRpcException e) {
			System.out.println("Caught XmlRpc exception: " + e.getMessage());
		}
	}
}
