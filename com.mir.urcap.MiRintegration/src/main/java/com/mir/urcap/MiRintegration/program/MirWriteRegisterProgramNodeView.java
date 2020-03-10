package com.mir.urcap.MiRintegration.program;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mir.urcap.MiRintegration.common.MirXmlRpcAsyncCommunicator;
import com.mir.urcap.MiRintegration.common.UIComponentFactory;
import com.mir.urcap.MiRintegration.common.XmlRpcCallback;
import com.mir.urcap.MiRintegration.installationnode.MirInstallationNodeContribution;
import com.mir.urcap.MiRintegration.style.Style;
import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;

public class MirWriteRegisterProgramNodeView implements 
	SwingProgramNodeView<MirWriteRegisterProgramNodeContribution> {
	
	private static final String INFO_TEXT = "<html>Write a PLC register in the MiR robot.<br/><br/>" +
			"Select the register you want to write and the value. " +
			"Registers 1-100 are integers and 101-200 are float values.";
	
	private JComboBox registerCmb;
	private JTextField valueInputField;
	
	private final UIComponentFactory uiFactory;
	private final Style style;
	private ContributionProvider<MirWriteRegisterProgramNodeContribution> contributionProvider;
	
	public MirWriteRegisterProgramNodeView(Style style) {
		this.style = style;
		this.uiFactory = new UIComponentFactory(style);
		
		registerCmb = new JComboBox();
		valueInputField = new JTextField();
	}

	@Override
	public void buildUI(JPanel panel, ContributionProvider<MirWriteRegisterProgramNodeContribution> provider) {
		this.contributionProvider = provider;
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(uiFactory.createInfoSection(INFO_TEXT));
		panel.add(uiFactory.createVerticalSpacing(5));
		panel.add(createInput());
	}
	
	private Box createInput() {
		Box section = Box.createHorizontalBox();
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		inputPanel.add(new JLabel("Register: "));
		
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
		
		inputPanel.add(uiFactory.createHorizontalSpacing());
		inputPanel.add(uiFactory.createHorizontalSpacing());
		inputPanel.add(uiFactory.createHorizontalSpacing());
		
		MouseAdapter valueAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Integer register = contributionProvider.get().getRegisterId();
				if (register <= 100) {
					KeyboardNumberInput<Integer> keyboard = contributionProvider.get().getKeyboardForIntValue();
					keyboard.setInitialValue(contributionProvider.get().getValueProviderForIntValue().get());
					keyboard.show(valueInputField, contributionProvider.get().getCallbackForIntValue());
				}
				else {
					KeyboardNumberInput<Double> keyboard = contributionProvider.get().getKeyboardForDoubleValue();
					keyboard.setInitialValue(contributionProvider.get().getValueProviderForDoubleValue().get());
					keyboard.show(valueInputField, contributionProvider.get().getCallbackForDoubleValue());
				}
				
			}
		};
		
		inputPanel.add(uiFactory.createTextField(valueInputField, "Value:", valueAdapter));
		
		section.add(inputPanel);
		
		return section;
	}
	
	public void updateRegistersComboBox() {
		List<Object> items = new ArrayList<Object>();
		items.addAll(contributionProvider.get().getRegisterLabels());
		
		items.add(0, "Select register");
		
		registerCmb.setModel(new DefaultComboBoxModel(items.toArray()));
		
		registerCmb.setSelectedItem(contributionProvider.get().getRegister());
	}
	
	public void setValue(String value) {
		valueInputField.setText(value);
	}
}
