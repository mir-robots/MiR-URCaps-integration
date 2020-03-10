package com.mir.urcap.MiRintegration.common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.mir.urcap.MiRintegration.style.Style;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

public class UIComponentFactory {
	
	private final float LINE_SPACING = 0.5f;
	private final float LEFT_INDENT = 0f;
	
	private final Style style;
	
	public UIComponentFactory(Style style) {
		this.style = style;
	}
	
	public Box createInfoSection(String infoText) {
		Box infoBox = Box.createHorizontalBox();
		infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel infoLabel = new JLabel(infoText);
		infoBox.add(infoLabel);

		return infoBox;
	}
	
	public JTextPane createTextPane(String info) {
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setLineSpacing(attributeSet, LINE_SPACING);
		StyleConstants.setLeftIndent(attributeSet, LEFT_INDENT);
		
		JTextPane pane = new JTextPane();
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setParagraphAttributes(attributeSet, false);
		pane.setText(info);
		pane.setEditable(false);
		pane.setMaximumSize(pane.getPreferredSize());
		
		return pane;
	}
	
	public Box createHeaderSection(String headerText) {
		Box section = Box.createHorizontalBox();
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JTextPane pane = createTextPane(headerText);
		pane.setBackground(section.getBackground());
		section.add(pane);
		
		return section;
	}
	
	public JTextField createIPInputField(final KeyboardTextInput keyboardTextInput,
										 final KeyboardInputCallback<String> callback,
										 final ValueProvider<String> initialValueProvider) {
		final JTextField inputField = new JTextField();
		
		inputField.setFocusable(false);
		inputField.setHorizontalAlignment(JTextField.CENTER);
		inputField.setPreferredSize(style.getInputFieldSize());
		inputField.setMinimumSize(style.getInputFieldSize());
		inputField.setMaximumSize(style.getInputFieldSize());
		
		inputField.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				keyboardTextInput.setInitialValue(initialValueProvider.get());
				keyboardTextInput.show(inputField, callback);
			}
		});
		
		return inputField;
	}
	
	public JLabel createSmallLabel(String label) {
		JLabel jLabel = new JLabel(label);

		jLabel.setPreferredSize(style.getSmallInputFieldLabelSize());
		jLabel.setMinimumSize(style.getSmallInputFieldLabelSize());
		jLabel.setMaximumSize(style.getSmallInputFieldLabelSize());

		return jLabel;
	}
	
	public JLabel createLabel(String label) {
		JLabel jLabel = new JLabel(label);

		jLabel.setPreferredSize(style.getSmallInputFieldSize());
		jLabel.setMinimumSize(style.getSmallInputFieldSize());
		jLabel.setMaximumSize(style.getSmallInputFieldSize());

		return jLabel;
	}
	
	public Box createStatusSection(JLabel label) {
		Box section = Box.createHorizontalBox();
		section.setAlignmentX(LEFT_INDENT);
		
		section.add(createSmallHorizontalSpacing());
		
		section.add(label);
		
		return section;
	}
	
	public Box createLabelBox(String info, JLabel label) {
		Box section = Box.createVerticalBox();
		section.setAlignmentY(Component.TOP_ALIGNMENT);
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		section.add(createHeaderSection(info));
		
		Box subsection = Box.createHorizontalBox();
		subsection.setAlignmentX(Component.LEFT_ALIGNMENT);
		subsection.add(createHorizontalSpacing());
		subsection.add(label);
		
		section.add(subsection);
				
		return section;
	}
	
	public JButton createCenteredButton(String text) {
		JButton button = new JButton(text);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		return button;
	}
	
	public <T> JTextField createNumberInputField(final KeyboardNumberInput<T> keyboardNumberInput,
		final KeyboardInputCallback<T> callback,
		final ValueProvider<T> initialValueProvider) {
			final JTextField inputField = new JTextField();
			
			inputField.setFocusable(false);
			inputField.setHorizontalAlignment(JTextField.RIGHT);
			inputField.setPreferredSize(style.getSmallInputFieldSize());
			inputField.setMinimumSize(style.getSmallInputFieldSize());
			inputField.setMaximumSize(style.getSmallInputFieldSize());
			
			inputField.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					keyboardNumberInput.setInitialValue(initialValueProvider.get());
					keyboardNumberInput.show(inputField, callback);
			}
		});
		
		return inputField;
	}
	
	public Box createTextField(JTextField textField, String text, MouseAdapter adapter) {
		Box box = Box.createHorizontalBox();
		
		JLabel label = new JLabel(text);
		label.setPreferredSize(new Dimension(80, 30));
		label.setMinimumSize(label.getPreferredSize());
		label.setMaximumSize(label.getPreferredSize());
		
		box.add(label);
		
		textField.setPreferredSize(style.getSmallInputFieldSize());
		textField.setMinimumSize(style.getSmallInputFieldSize());
		textField.setMaximumSize(style.getSmallInputFieldSize());
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addMouseListener(adapter);
		
		box.add(textField);
		
		return box;
	}
	
	public Component createSmallHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(style.getSmallHorizontalSpacing(), 0));
	}

	public Component createHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing(), 0));
	}
	
	public Component createHorizontalSpacing(int n) {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing() * n, 0));
	}

	public Component createSmallVerticalSpacing() {
		return Box.createRigidArea(new Dimension(0, style.getSmallVerticalSpacing()));
	}

	public Component createVerticalSpacing() {
		return Box.createRigidArea(new Dimension(0, style.getVerticalSpacing()));
	}

	public Component createVerticalSpacing(int n) {
		return Box.createRigidArea(new Dimension(0, style.getVerticalSpacing() * n));
	}

}
