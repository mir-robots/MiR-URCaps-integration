package com.mir.urcap.MiRintegration.program;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mir.urcap.MiRintegration.common.UIComponentFactory;
import com.mir.urcap.MiRintegration.style.Style;
import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;

public class MirReadRegisterProgramNodeView implements SwingProgramNodeView<MirReadRegisterProgramNodeContribution> {
	
	private static final String INFO_TXT = "<html>Read a PLC register from the MiR robot.<br><br>"
			+ "Select the variable you want to read the register to and the register you want to read.<br>"
			+ "Registers 1-100 are integers and 101-200 are float values.<br>";
	private static final String INFO_2_TXT = "Input variable name and press the \"Create New\" button";
	private static final String VARIABLE_TXT = "<html><b>Variable</b></html>";
	private static final String REGISTER_TXT = "<html><b>Register</b></html>";
	private static final String EQUAL_TXT = "<html><b>=</b></html>";
	private static final String NEW_VARIABLE_TXT = "Create new";
	
	private JComboBox variablesCmb;
	private JComboBox registerCmb;
	private JTextField newVariableInputField;
	private JButton newVariableBtn;
	private JLabel errorLabel;
	private final ImageIcon errorIcon;
	
	private final UIComponentFactory uiFactory;
	private final Style style;
	private ContributionProvider<MirReadRegisterProgramNodeContribution> contributionProvider;
	
	
	public MirReadRegisterProgramNodeView(Style style) {
		this.style = style;
		this.uiFactory = new UIComponentFactory(style);
		
		variablesCmb = new JComboBox();
		registerCmb = new JComboBox();
		newVariableInputField = new JTextField();
		newVariableBtn = new JButton(NEW_VARIABLE_TXT);
		errorLabel = new JLabel();
		errorIcon = getErrorImage();
	}
	
	@Override
	public void buildUI(JPanel panel, ContributionProvider<MirReadRegisterProgramNodeContribution> provider) {
		this.contributionProvider = provider;
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(uiFactory.createInfoSection(INFO_TXT));
		
		panel.add(uiFactory.createVerticalSpacing(5));
		
		panel.add(createAssignment());
		
		panel.add(uiFactory.createVerticalSpacing(5));
		
		panel.add(uiFactory.createInfoSection(INFO_2_TXT));
		
		panel.add(createButtonBox());
		
		panel.add(uiFactory.createVerticalSpacing(5));
				
		panel.add(createErrorLabel());
		
		panel.add(Box.createVerticalGlue());
	}
	
	private Box createAssignment() {
		Box box = Box.createVerticalBox();
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		
		Box labelBox = Box.createHorizontalBox();
		labelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		labelBox.add(uiFactory.createHorizontalSpacing(1));
		labelBox.add(uiFactory.createLabel(VARIABLE_TXT));
		labelBox.add(uiFactory.createHorizontalSpacing(32));
		labelBox.add(uiFactory.createLabel(REGISTER_TXT));
		
		box.add(labelBox);
		
		box.add(createInput());
		
		return box;
	}
	
	
	
	private Component createInput() {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		variablesCmb.setFocusable(false);
		variablesCmb.setPreferredSize(style.getInputFieldSize());
		variablesCmb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (e.getItem() instanceof Variable) {
						contributionProvider.get().setVariable((Variable) e.getItem());
					} else {
						contributionProvider.get().removeVariable();
					}
				}
			}
		});
		inputPanel.add(variablesCmb);
		
		inputPanel.add(new JLabel(EQUAL_TXT));
		
		inputPanel.add(new JLabel("getMirRegister("));
		
		registerCmb.setFocusable(false);
		registerCmb.setPreferredSize(new Dimension(200, 30));
		registerCmb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getItem() instanceof String) {
					contributionProvider.get().setRegister((String) e.getItem()); 
				} else {
					contributionProvider.get().removeRegister();
				}
			}
		});
		inputPanel.add(registerCmb);
		
		inputPanel.add(new JLabel(")"));
		
		box.add(inputPanel);
		
		return box;
	}
	
	private Box createButtonBox() {
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        newVariableInputField.setFocusable(false);
        newVariableInputField.setPreferredSize(style.getInputFieldSize());
        newVariableInputField.setMaximumSize(newVariableInputField.getPreferredSize());
        newVariableInputField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                KeyboardTextInput keyboard = contributionProvider.get().getKeyboardForNewVariable();
                keyboard.setInitialValue(contributionProvider.get().getValueProviderForNewVariable().get());
                keyboard.show(newVariableInputField, contributionProvider.get().getCallbackForNewVariable());
            }
        });

        horizontalBox.add(newVariableInputField);
        horizontalBox.add(uiFactory.createHorizontalSpacing());

        newVariableBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clearErrors();
                //Create a global variable with an initial value and store it in the data model to make it available to all program nodes.
                GlobalVariable variable = contributionProvider.get().createGlobalVariable(newVariableInputField.getText());
                contributionProvider.get().setVariable(variable);
                updateVariablesComboBox();
            }
        });

        horizontalBox.add(newVariableBtn);
        return horizontalBox;
    }
	
	private Box createErrorLabel() {
        Box infoBox = Box.createHorizontalBox();
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        infoBox.add(errorLabel);
        return infoBox;
    }

	
	private void updateVariablesComboBox() {
		List<Object> items = new ArrayList<Object>();
		items.addAll(contributionProvider.get().getGlobalVariables());
		
		Collections.sort(items, new Comparator<Object>() {
			@Override
			public int compare(Object arg0, Object arg1) {
				if (arg0.toString().toLowerCase().compareTo(arg1.toString().toLowerCase()) == 0) {
					return arg0.toString().compareTo(arg1.toString());
				} else {
					return arg0.toString().toLowerCase().compareTo(arg1.toString().toLowerCase());
				}
			}
		});
		
		items.add(0, "Select variable for the register");
		
		variablesCmb.setModel(new DefaultComboBoxModel(items.toArray()));
		
		Variable selectedVar = contributionProvider.get().getSelectedVariable();
		if (selectedVar != null) {
			variablesCmb.setSelectedItem(selectedVar);
		}
	}
	
	private void updateRegistersComboBox() {
		List<Object> items = new ArrayList<Object>();
		items.addAll(contributionProvider.get().getRegisterLabels());

		items.add(0, "Select register");
		
		registerCmb.setModel(new DefaultComboBoxModel(items.toArray()));
		
		registerCmb.setSelectedItem(contributionProvider.get().getRegister());
	}
	
	public void update() {
		clearInputVariableName();
		clearErrors();
		updateVariablesComboBox();
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				updateRegistersComboBox();				
			}
		});
	}
	
	public void setNewVariable(String variable) {
		newVariableInputField.setText(variable);
	}
	
	public void setError(final String message) {
        errorLabel.setText("<html>Error: Could not create variable<br>" + message + "</html>");
        errorLabel.setIcon(errorIcon);
        errorLabel.setVisible(true);
    }

    private void clearInputVariableName() {
        newVariableInputField.setText("");
    }

    private void clearErrors() {
        errorLabel.setVisible(false);
    }
	
	private ImageIcon getErrorImage() {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/icons/warning-bigger.png"));
            return new ImageIcon(image);
        } catch (IOException e) {
            // Should not happen.
            throw new RuntimeException("Unexpected exception while loading icon.", e);
        }
    }

}
