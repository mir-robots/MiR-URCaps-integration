package com.mir.urcap.MiRintegration.installationnode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.ConnectException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mir.urcap.MiRintegration.common.UIComponentFactory;
import com.mir.urcap.MiRintegration.style.Style;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

public class MirInstallationNodeView implements SwingInstallationNodeView<MirInstallationNodeContribution> {
	
	private static final String FEATURES_TXT = "URCap features";
	private static final String SET_MIR_IP_TXT = "MiR robot IP";
	private static final String ENABLE_TXT = "Enable MiR URCap";
	private static final String INFO_TXT = "<html>This is the URCap for the integration between the MiR and the UR "
			+ "robots, which allows the users to read and write registers from the MiR robot through mission nodes "
			+ "and which includes a toolbar displaying the status of the MiR robot as well as to Pause or Continue "
			+ "the mission it is running.<br><br>"
			+ "The \"MiR robot IP\" field allows the user to set the IP of the MiR robot this UR arm is going to "
			+ "interact with. Please be aware that the default IP of the MiR is 192.168.12.20, and that it only "
			+ "needs to be modified if both robots are connected using something else than the top Ethernet port "
			+ "available in the MiR.<br><br>"
			+ "The \"Enable MiR URCap\" checkbox allows the user to start or stop the script that runs "
			+ "in the background in order to stablish communication with the MiR robot. In order for the functionalities "
			+ "of this URCap to work, the service needs to be running. It also adds the <i>getMirPlcRegister</i> and the "
			+ "<i>writeMirPlcRegister</i> functions.<br><br>"
			+ "<strong>Important: Please be aware that the UR needs to be in remote mode for the MiR robot to be able "
			+ "to start programs on the UR arm from its mission interface.</strong><br>"
			+ "For more information please refer to the <i>MiR100/200 Universal Robots Interface Operating guide 2.0</i> "
			+ "available on <a ref=\"\">www.mobile-industrial-robots.com</a></html>";
	private static final String FUNCTIONS_TXT = "<html>Checking this box enables the background MiR service and adds these "
			+ "functions to the system<br><br>"
			+ "<i>getMirPlcRegister(register)</i> - Reads the value of the register "
			+ "passed as a parameter from the MiR robot and returns it<br><br>"
			+ "<i>writeMirPlcRegister(register, value)</i> - Writes the value from the second parameter into the register "
			+ "from the first parameter in the MiR robot</html>";
	
	private final UIComponentFactory uiFactory;
	private final Style style;
	
	private JTextField ipAddressField;
	private JCheckBox runDaemonCb;
	private JLabel daemonStatus;
	private JCheckBox addFunctionsCb;
	
	public MirInstallationNodeView(Style style) {
		this.style = style;
		this.uiFactory = new UIComponentFactory(style);
	}
	
	@Override
	public void buildUI(JPanel panel, final MirInstallationNodeContribution contribution) {
		panel.setLayout(new GridLayout(1, 2));
		
		Box content = Box.createVerticalBox();
		content.setBorder(BorderFactory.createEmptyBorder(0, style.getContentIndent(), 0, 0));
		content.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.setAlignmentY(Component.TOP_ALIGNMENT);
		
		content.add(uiFactory.createHeaderSection(SET_MIR_IP_TXT));
		content.add(createIPAddressBox(contribution));
		
		content.add(uiFactory.createVerticalSpacing(6));
				
		content.add(uiFactory.createHeaderSection(FEATURES_TXT));
		content.add(createFeaturesBox(contribution));
		
		panel.add(content);
		
		
		panel.add(createInfoBox());
	}
	
	private Component createIPAddressBox(final MirInstallationNodeContribution contribution) {
		Box section = Box.createHorizontalBox();
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		ipAddressField = uiFactory.createIPInputField(
				contribution.getKeyboardForMirIp(),
				contribution.getCallbackForMirIp(),
				contribution.getValueProviderForMirIp());
		
		section.add(ipAddressField);
		
		return section;				
	}
	
	private Component createFeaturesBox(final MirInstallationNodeContribution contribution) {
		Box section = Box.createVerticalBox();
		section.setAlignmentX(Component.LEFT_ALIGNMENT);
		section.setAlignmentY(Component.TOP_ALIGNMENT);
		
		runDaemonCb = createEnableCheckbox(contribution);
		section.add(runDaemonCb);
		
		daemonStatus = new JLabel();
		section.add(uiFactory.createStatusSection(daemonStatus));
				
		section.add(uiFactory.createSmallVerticalSpacing());
		
		Box functions = Box.createHorizontalBox();
		functions.setAlignmentX(Component.LEFT_ALIGNMENT);
		functions.setAlignmentY(Component.TOP_ALIGNMENT);
		
		functions.add(uiFactory.createHorizontalSpacing());
		
		functions.add(new JLabel(FUNCTIONS_TXT));
		
		section.add(functions);
		
		return section;
	}
	
	private Component createInfoBox() {
		Box info = Box.createVerticalBox();
		info.setBorder(BorderFactory.createEmptyBorder(0, style.getContentIndent(), 0, 0));
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		info.setAlignmentY(Component.TOP_ALIGNMENT);
		
		info.add(new JLabel(INFO_TXT));
		
		info.add(uiFactory.createVerticalSpacing(2));
		
		JLabel picLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/mir_logo.png")));
		
		info.add(picLabel);
		
		return info;
	}
	
	public void setMirIp(String value) {
		ipAddressField.setText(value);
	}
	
	public void setRunDaemonSelected(boolean enabled) {
		runDaemonCb.setSelected(enabled);
	}
	
	public void setDaemonStatus(String status) {
		daemonStatus.setText(status);
	}
	
	private JCheckBox createEnableCheckbox(final MirInstallationNodeContribution contribution) {
		JCheckBox checkBox = new JCheckBox(ENABLE_TXT);
		checkBox.setFocusPainted(false);
		
		checkBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					contribution.runDaemonChanged(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					contribution.runDaemonChanged(false);
				}
			}
		});
		
		return checkBox;
	}

}
