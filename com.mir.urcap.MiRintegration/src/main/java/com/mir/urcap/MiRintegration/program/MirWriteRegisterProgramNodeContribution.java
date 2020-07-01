package com.mir.urcap.MiRintegration.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mir.urcap.MiRintegration.common.MirXmlRpcAsyncCommunicator;
import com.mir.urcap.MiRintegration.common.RegisterValidator;
import com.mir.urcap.MiRintegration.common.ValueProvider;
import com.mir.urcap.MiRintegration.common.XmlRpcCallback;
import com.mir.urcap.MiRintegration.installationnode.MirInstallationNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidationFactory;
import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidator;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.variable.Variable;

public class MirWriteRegisterProgramNodeContribution implements ProgramNodeContribution {
	
	private final static String REGISTER_KEY = "register";
	private final static String VALUE_KEY = "value";
	
	private final static String DEFAULT_REGISTER = "Select register";
	private final static Integer DEFAULT_VALUE = 0;
	private final static Double DEFAULT_DOUBLE_VALUE = 0.0;
	
	private final KeyboardInputFactory keyboardFactory;
	private final InputValidationFactory inputValidatorFactory;
	private final InputValidator<Integer> registerValidator;
	
	private final ProgramAPI programApi;
	private final MirWriteRegisterProgramNodeView view;
	private final DataModel model;
	
	private final LinkedHashMap<String, Integer> registersMap;
	
	public MirWriteRegisterProgramNodeContribution(ProgramAPIProvider apiProvider, 
			MirWriteRegisterProgramNodeView view, DataModel model) {
		this.programApi = apiProvider.getProgramAPI();
		this.view = view;
		this.model = model;
		
		keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		inputValidatorFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getInputValidationFactory();
		registerValidator = new RegisterValidator();
		
		registersMap = new LinkedHashMap<String, Integer>();
	}

	@Override
	public void openView() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				view.updateRegistersComboBox();
				if (getRegisterId() <= 100) {
					view.setValue(getIntValue().toString());
				}
				else {
					view.setValue(getDoubleValue().toString());
				}								
			}
		});		
	}

	@Override
	public void closeView() {		
	}

	@Override
	public String getTitle() {
		String register = getRegister();
		String title = "Set MiR register " + register + " to ";
		if (getRegisterId() <= 100) {
			return title + getIntValue();
		}
		else {
			return title + getDoubleValue();
		}
	}

	@Override
	public boolean isDefined() {
		if (isInstallationDefined()) {			
			String register = getRegister();
			
			if (registersMap.containsKey(register)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		String script = getXmlRpcScriptHandle() + ".write_register(" + getRegisterId() + ", ";
		if (getRegisterId() <= 100) {
			script += getIntValue();
		} else {
			script += getDoubleValue();
		}
		script += ")";
		writer.appendLine(script);
	}
	
	public Collection<Variable> getGlobalVariables() {
		return programApi.getVariableModel().get(new Filter<Variable>() {
			@Override
			public boolean accept(Variable element) {
				return element.getType().equals(Variable.Type.GLOBAL) || element.getType().equals(Variable.Type.VALUE_PERSISTED);
			}
		});
	}
	
	public String getRegister() {
		return model.get(REGISTER_KEY, DEFAULT_REGISTER);
	}
	
	public Integer getRegisterId() {
		String register = getRegister();
		if (registersMap.containsKey(register)) {
			return registersMap.get(register);
		} else {
			return 101;
		}
	}
	
	public void setRegister(final String register) {
		programApi.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(REGISTER_KEY, register);
			}
		});
	}
	
	public void removeRegister() {
		programApi.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.remove(REGISTER_KEY);
			}
		});
	}
	
	public Integer getIntValue() {
		return model.get(VALUE_KEY, DEFAULT_VALUE);
	}
	
	public void setIntValue(Integer value) {
		model.set(VALUE_KEY, value);
	}
	
	public Double getDoubleValue() {
		return model.get(VALUE_KEY, DEFAULT_DOUBLE_VALUE);
	}
	
	public void setDoubleValue(Double value) {
		model.set(VALUE_KEY, value);
	}
	
	private boolean isInstallationDefined() {
		return programApi.getInstallationNode(MirInstallationNodeContribution.class).isInstallationReady();
	}
	
	private String getXmlRpcScriptHandle() {
		return programApi.getInstallationNode(MirInstallationNodeContribution.class).getXmlRpcScriptHandle();
	}
	
	public KeyboardNumberInput<Integer> getKeyboardForRegister() {
		KeyboardNumberInput<Integer> keyboard = keyboardFactory.createIntegerKeypadInput();
		keyboard.setErrorValidator(registerValidator);
		
		return keyboard;
	}
	
	public KeyboardNumberInput<Integer> getKeyboardForIntValue() {
		KeyboardNumberInput<Integer> keyboard = keyboardFactory.createIntegerKeypadInput();
		
		return keyboard;
	}
	
	public KeyboardInputCallback<Integer> getCallbackForIntValue() {
		return new KeyboardInputCallback<Integer>() {
			@Override
			public void onOk(Integer value) {
				setIntValue(value);
				view.setValue(getIntValue().toString());
			}
		};
	}
	
	public ValueProvider<Integer> getValueProviderForIntValue() {
		return new ValueProvider<Integer>() {
			@Override
			public Integer get() {
				return getIntValue();
			}
		};
	}
	
	public KeyboardNumberInput<Double> getKeyboardForDoubleValue() {
		KeyboardNumberInput<Double> keyboard = keyboardFactory.createDoubleKeypadInput();
		
		return keyboard;
	}
	
	public KeyboardInputCallback<Double> getCallbackForDoubleValue() {
		return new KeyboardInputCallback<Double>() {
			@Override
			public void onOk(Double value) {
				setDoubleValue(value);
				view.setValue(getDoubleValue().toString());
			}
		};
	}
	
	public ValueProvider<Double> getValueProviderForDoubleValue() {
		return new ValueProvider<Double>() {
			@Override
			public Double get() {
				return getDoubleValue();
			}
		};
	}

	public ArrayList<String> getRegisterLabels() {
		ArrayList<String> labels = new ArrayList<String>();
		
		getXmlRpcHandle().get_register_labels(new XmlRpcCallback<String>() {
			@Override
			public void getResult(String result) {
				try {
					JsonArray registers = new JsonParser().parse(result).getAsJsonArray();
					if (registers.size() == 200) {
						registersMap.clear();
					}
					for (int i=0; i<registers.size(); i++) {
						JsonObject register = registers.get(i).getAsJsonObject();
						String label = register.get("label").getAsString();
						if (label.isEmpty()) {
							label = "PLC register " + register.get("id").getAsString();
						}
						registersMap.put(label, register.get("id").getAsInt());
					}					
				} catch (JsonParseException e) {
					System.out.println("The answer from the registers endpoint is not a valid JSON");
				}
			}
		});
		
		while (registersMap.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		labels.addAll(registersMap.keySet());
		
		return labels;
	}
	
	private MirXmlRpcAsyncCommunicator getXmlRpcHandle() {
		return programApi.getInstallationNode(MirInstallationNodeContribution.class).getXmlRpcHandle();
	}
}
