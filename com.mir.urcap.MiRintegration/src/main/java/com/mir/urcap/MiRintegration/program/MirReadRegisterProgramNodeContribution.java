package com.mir.urcap.MiRintegration.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mir.urcap.MiRintegration.common.MirXmlRpcAsyncCommunicator;
import com.mir.urcap.MiRintegration.common.ValueProvider;
import com.mir.urcap.MiRintegration.common.XmlRpcCallback;
import com.mir.urcap.MiRintegration.installationnode.MirInstallationNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.value.expression.InvalidExpressionException;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableException;
import com.ur.urcap.api.domain.variable.VariableFactory;

public class MirReadRegisterProgramNodeContribution implements ProgramNodeContribution {
	
	private final static String SELECTED_VAR_KEY = "selectedVar";
	private final static String REGISTER_KEY = "register";
	private final static String NEW_VARIABLE_KEY = "newVariable";
	
	private final static String DEFAULT_REGISTER = "Select register";
	private final static String NEW_VARIABLE_DEFAULT = "";
	
	private final VariableFactory variableFactory;
	private final KeyboardInputFactory keyboardFactory;
	
	private final ProgramAPI programApi;
	private final MirReadRegisterProgramNodeView view;
	private final DataModel model;
	
	private final LinkedHashMap<String, Integer> registersMap;
	
	public MirReadRegisterProgramNodeContribution(ProgramAPIProvider apiProvider,
			MirReadRegisterProgramNodeView view, DataModel model) {
		this.programApi = apiProvider.getProgramAPI();
		this.view = view;
		this.model = model;
		
		variableFactory = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
		keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		
		registersMap = new LinkedHashMap<String, Integer>();
	}

	@Override
	public void openView() {
		view.update();
		view.setNewVariable(getNewVariable());
	}

	@Override
	public void closeView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		return "Read MiR register " + getRegister();
	}

	@Override
	public boolean isDefined() {
		Collection<Variable> globalVariables = getGlobalVariables();
		Variable variable = getSelectedVariable();
		
		String register = getRegister();
		
		if (globalVariables.contains(variable) && registersMap.containsKey(register)) {
			return true;
		}
		
		return false;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		Variable variable = getSelectedVariable();
		if (variable != null) {
			String resolvedVariableName = writer.getResolvedVariableName(variable);
			Integer register = registersMap.get(getRegister());
			writer.appendLine(resolvedVariableName + "=getMirPlcRegister(" + register + ")");
		}
	}
	
	public Collection<Variable> getGlobalVariables() {
		return programApi.getVariableModel().get(new Filter<Variable>() {
			@Override
			public boolean accept(Variable element) {
				return element.getType().equals(Variable.Type.GLOBAL) || element.getType().equals(Variable.Type.VALUE_PERSISTED);
			}
		});
	}
	
	public Variable getSelectedVariable() {
		return model.get(SELECTED_VAR_KEY, (Variable) null);
	}
	
	public void setVariable(final Variable variable) {
		programApi.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(SELECTED_VAR_KEY, variable);
			}
		});
	}

	public void removeVariable() {
		programApi.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.remove(SELECTED_VAR_KEY);
			}
		});
	}
	
	public String getRegister() {
		return model.get(REGISTER_KEY, DEFAULT_REGISTER);
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
	
	public String getNewVariable() {
		return model.get(NEW_VARIABLE_KEY, NEW_VARIABLE_DEFAULT);
	}
	
	public GlobalVariable createGlobalVariable(String variableName) {
		GlobalVariable variable = null;
		try {
			variable = variableFactory.createGlobalVariable(variableName, programApi.getValueFactoryProvider().createExpressionBuilder().append("0").build());
		} catch (VariableException e) {
			view.setError(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (InvalidExpressionException e) {
			view.setError(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return variable;
	}
	
	public void setNewVariable(String variable) {
		model.set(NEW_VARIABLE_KEY, variable);
	}
	
	public KeyboardTextInput getKeyboardForNewVariable() {
		KeyboardTextInput keyboard = keyboardFactory.createStringKeyboardInput();
		
		return keyboard;
	}
	
	public KeyboardInputCallback<String> getCallbackForNewVariable() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				view.setNewVariable(value);
			}
		};
	}
	
	public ValueProvider<String> getValueProviderForNewVariable() {
		return new ValueProvider<String>() {
			@Override
			public String get() {
				return getNewVariable();
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
