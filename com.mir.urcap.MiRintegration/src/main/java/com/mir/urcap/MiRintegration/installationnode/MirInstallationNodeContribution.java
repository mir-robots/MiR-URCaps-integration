package com.mir.urcap.MiRintegration.installationnode;

import com.ur.urcap.api.contribution.DaemonContribution.State;

import java.util.Timer;
import java.util.TimerTask;

import com.mir.urcap.MiRintegration.common.IpAddressValidator;
import com.mir.urcap.MiRintegration.common.MirDaemonService;
import com.mir.urcap.MiRintegration.common.MirXmlRpcAsyncCommunicator;
import com.mir.urcap.MiRintegration.common.ValueProvider;
import com.mir.urcap.MiRintegration.common.XmlRpcCallback;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.function.Function;
import com.ur.urcap.api.domain.function.FunctionException;
import com.ur.urcap.api.domain.function.FunctionModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidator;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

public class MirInstallationNodeContribution implements InstallationNodeContribution {
	
	private static final String MIR_IP_KEY = "mirIp";
	private static final String RUN_DAEMON_KEY = "runDaemon";
	private static final String INITIALIZED = "initialized";
	
	private static final String MIR_IP_DEFAULT = "192.168.12.20";
	private static final Boolean RUN_DAEMON_DEFAULT = true;
	private static final String DAEMON_STATUS_RUNNING = "MiR background service is running";
	private static final String DAEMON_STATUS_STOPPED = "MiR background service is stopped";
	private static final String DAEMON_STATUS_FAILED = "MiR background service is in error";
	private static final String READ_REGISTER_FUNCTION_NAME = "getMirPlcRegister";
	private static final String READ_REGISTER_PARAM_NAME = "register";
	private static final String WRITE_REGISTER_FUNCTION_NAME = "setMirPlcRegister";
	private static final String WRITE_REGISTER_PARAM_NAME = "register";
	private static final String WRITE_REGISTER_VALUE_NAME = "value";
	
	private final InstallationAPIProvider apiProvider;
	private final MirInstallationNodeView view;
	private final DataModel model;
	private final MirDaemonService daemonService;
	
	private final KeyboardInputFactory keyboardFactory;
	private final InputValidator<String> mirIpValidator;
	
	private Timer uiTimer;
	
	private static final String XMLRPCHandle = "mirRestHandle";
	
	private final MirXmlRpcAsyncCommunicator mirXmlRpc = new MirXmlRpcAsyncCommunicator();
	
	public MirInstallationNodeContribution(InstallationAPIProvider apiProvider, MirInstallationNodeView view,
			DataModel model, MirDaemonService daemonService) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.daemonService = daemonService;
		
		keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		mirIpValidator = new IpAddressValidator();
				
		initializeDefaults();
		applyDaemonState();
		applyFunctionsChange(getRunDaemonEnabled());
		setMirIp(getMirIp());
	}
	
	private void initializeDefaults() {
		if (model.get(INITIALIZED, false)) {
			return;
		}
		model.set(INITIALIZED, true);
		
		setMirIp(getMirIp());
		runDaemonChanged(getRunDaemonEnabled());
	}
	
	@Override
	public void openView() {
		view.setMirIp(getMirIp());
		view.setRunDaemonSelected(getRunDaemonEnabled());
		view.setDaemonStatus(DAEMON_STATUS_RUNNING);
		
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				view.setDaemonStatus(getDaemonStatusString());
			}
		}, 0, 500);
	}

	@Override
	public void closeView() {
		if (uiTimer != null) {
			uiTimer.cancel();
		}
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		if (isInstallationReady() && isDaemonRunning()) {
			writer.appendLine("# Connect to MiR REST XML RPC server");
			writer.appendLine(XMLRPCHandle + " = rpc_factory(\"xmlrpc\", \"http://127.0.0.1:34567\")");
			
			// Read register function
			writer.appendLine("def " + READ_REGISTER_FUNCTION_NAME + "(" + READ_REGISTER_PARAM_NAME + "):");
			writer.ifCondition("register < 1 or register > 200");
			writer.appendLine("popup(\"Register for getMirPlcRegister must be between 1 and 200\", title=\"MiR integration\", blocking=True, error=True)");
			writer.appendLine("return 0");
			writer.end();
			writer.appendLine("return " + XMLRPCHandle + ".get_register(" + READ_REGISTER_PARAM_NAME + ")");
			writer.end();
			
			// Write register function
			writer.appendLine("def " + WRITE_REGISTER_FUNCTION_NAME + "(" + WRITE_REGISTER_PARAM_NAME + ", " + WRITE_REGISTER_VALUE_NAME + "):");
			writer.ifCondition("register < 1 or register > 200");
			writer.appendLine("popup(\"Register for getMirPlcRegister must be between 1 and 200\", title=\"MiR integration\", blocking=True, error=True)");
			writer.appendLine("return 0");
			writer.end();
			writer.appendLine("return " + XMLRPCHandle + ".write_register(" + WRITE_REGISTER_PARAM_NAME + ", " + WRITE_REGISTER_VALUE_NAME + ")");
			writer.end();
		} else if (isInstallationReady()) {
			writer.appendLine("popup(\"Daemon should be running, but is not...\"," + 
					"\"MiR integration\", blocking=True, error=True");
		}
	}
	
	private void addFunction(String name, String... argumentNames) {
		FunctionModel functionModel = apiProvider.getInstallationAPI().getFunctionModel();
		if (functionModel.getFunction(name) == null) {
			try {
				functionModel.addFunction(name, argumentNames);
			} catch (FunctionException e) {
				System.out.println("Exception trying to add function: " + e.getMessage());
			}
		}
	}
	
	private void removeFunction(String name) {
		FunctionModel functionModel = apiProvider.getInstallationAPI().getFunctionModel();
		Function function = functionModel.getFunction(READ_REGISTER_FUNCTION_NAME);
		if (function != null) {
			functionModel.removeFunction(function);
		} else {
			System.out.println("Can not remove function. It doesn't exist");
		}
	}
	
	public void setMirIp(String value) {
		model.set(MIR_IP_KEY, value);
		mirXmlRpc.set_ip(value);
		System.out.println("Setting IP to: " + value);
	}
	
	public String getMirIp() {
		return model.get(MIR_IP_KEY, MIR_IP_DEFAULT);
	}
	
	public Boolean getRunDaemonEnabled() {
		return model.get(RUN_DAEMON_KEY, RUN_DAEMON_DEFAULT);
	}
	
	public MirXmlRpcAsyncCommunicator getXmlRpcHandle() {
		return mirXmlRpc;
	}
	
	public String getXmlRpcScriptHandle() {
		return XMLRPCHandle;
	}
	
	private boolean shouldRunDaemon() {
		return model.get(RUN_DAEMON_KEY, RUN_DAEMON_DEFAULT);
	}
	
	public boolean isInstallationReady() {
		return shouldRunDaemon();
	}
	
	private void applyDaemonState() {
		if(shouldRunDaemon()) {
			this.daemonService.getDaemonContribution().start();
		}
		else {
			this.daemonService.getDaemonContribution().stop();
		}
	}
	
	public String getDaemonStatusString() {
		State state = this.daemonService.getDaemonContribution().getState();
		if (state.equals(State.RUNNING)) {
			return DAEMON_STATUS_RUNNING;
		} else if (state.equals(State.STOPPED)) {
			return DAEMON_STATUS_STOPPED;
		} else {
			return DAEMON_STATUS_FAILED;
		}
	}
	
	public boolean isDaemonRunning() {
		return State.RUNNING.equals(this.daemonService.getDaemonContribution().getState());
	}
	
	public void runDaemonChanged(boolean value) {
		model.set(RUN_DAEMON_KEY, value);
		applyDaemonState();
		applyFunctionsChange(value);
	}
	
	private void applyFunctionsChange(boolean value) {
		if (value) {
			addFunction(READ_REGISTER_FUNCTION_NAME, READ_REGISTER_PARAM_NAME);
		} else {
			removeFunction(READ_REGISTER_FUNCTION_NAME);
		}
	}
	
	public KeyboardTextInput getKeyboardForMirIp() {
		KeyboardTextInput keyboard = keyboardFactory.createIPAddressKeyboardInput();
		keyboard.setErrorValidator(mirIpValidator);
		
		return keyboard;
	}
	
	public KeyboardInputCallback<String> getCallbackForMirIp() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				setMirIp(value);
				view.setMirIp(getMirIp());
			}
		};
	}
	
	public ValueProvider<String> getValueProviderForMirIp() {
		return new ValueProvider<String>() {
			@Override
			public String get() {
				return getMirIp();
			}
		};
	}
}

