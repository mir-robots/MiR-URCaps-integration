package com.mir.urcap.MiRintegration.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mir.urcap.MiRintegration.common.MirXmlRpcAsyncCommunicator;
import com.mir.urcap.MiRintegration.common.StatusThread;
import com.mir.urcap.MiRintegration.common.UIComponentFactory;
import com.mir.urcap.MiRintegration.installationnode.MirInstallationNodeContribution;
import com.mir.urcap.MiRintegration.style.Style;
import com.mir.urcap.MiRintegration.style.V3Style;
import com.mir.urcap.MiRintegration.style.V5Style;
import com.ur.urcap.api.contribution.toolbar.ToolbarAPIProvider;
import com.ur.urcap.api.contribution.toolbar.ToolbarContext;
import com.ur.urcap.api.contribution.toolbar.swing.SwingToolbarContribution;
import com.ur.urcap.api.domain.SystemAPI;
 
public class MirToolbarContribution implements SwingToolbarContribution {
	
	private final static String BATTERY_PERCENTAGE_TXT = "Battery percentage:";
	private final static String STATE_TXT = "Robot state:";
	private final static String CURRENT_MISSION_TXT = "Current mission:";
	private final static String MISSION_TXT = "Mission state:";
	private final static String PLAY_BUTTON_TXT = "Play";
	private final static String PAUSE_BUTTON_TXT = "Pause";
	private final static Integer STATUS_VERTICAL_SPACING = 1;
	private final static String DEFAULT_VALUE_TXT = "-";
	private final static String CONTROL_PANEL_HEADER_TXT = "MiR control";
	private final static Integer CONTROL_VERTICAL_SPACING = 1;
	
	private final ToolbarContext context;
	private final ToolbarAPIProvider apiProvider;
	private final Style style;
	private final UIComponentFactory uiFactory;
	
	private JLabel batteryLabel;
	private JLabel stateLabel;
	private JLabel currentMissionLabel;
	private JLabel missionLabel;
	private JButton continueButton;
	private JButton pauseButton;
	
	private final StatusThread statusThread;
	private final Timer statusTimer;
	
	public MirToolbarContribution(ToolbarContext context) {
		this.context = context;
		this.apiProvider = context.getAPIProvider();
		
		SystemAPI systemAPI = context.getAPIProvider().getSystemAPI();
		this.style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		this.uiFactory = new UIComponentFactory(style);
		
		statusThread = new StatusThread(getXmlRpc());
		statusThread.start();
		
		statusTimer = createTimer();
	}

	@Override
	public void buildUI(JPanel jPanel) {
		jPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(1, 0, 10, 0);
		jPanel.add(createControlPanel(), gbc);
				
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.insets = new Insets(10, 0, 1, 0);
		jPanel.add(createStatusPanel(), gbc);	
	}
	
	private JPanel createStatusPanel() {
		JPanel status_panel = new JPanel();
		BoxLayout status_layout = new BoxLayout(status_panel, BoxLayout.Y_AXIS);
		status_panel.setLayout(status_layout);
		status_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		status_panel.setPreferredSize(new Dimension(500, 300));
		status_panel.setMaximumSize(status_panel.getPreferredSize());
		status_panel.setMinimumSize(status_panel.getPreferredSize());
		
		batteryLabel = new JLabel(DEFAULT_VALUE_TXT);
		status_panel.add(uiFactory.createLabelBox(BATTERY_PERCENTAGE_TXT, batteryLabel));
		
		status_panel.add(uiFactory.createVerticalSpacing(STATUS_VERTICAL_SPACING));
		
		stateLabel = new JLabel(DEFAULT_VALUE_TXT);
		status_panel.add(uiFactory.createLabelBox(STATE_TXT, stateLabel));
		
		status_panel.add(uiFactory.createVerticalSpacing(STATUS_VERTICAL_SPACING));
		
		currentMissionLabel = new JLabel(DEFAULT_VALUE_TXT);
		status_panel.add(uiFactory.createLabelBox(CURRENT_MISSION_TXT, currentMissionLabel));
		
		status_panel.add(uiFactory.createVerticalSpacing(STATUS_VERTICAL_SPACING));
		
		missionLabel = new JLabel(DEFAULT_VALUE_TXT);
		status_panel.add(uiFactory.createLabelBox(MISSION_TXT, missionLabel));
				
		return status_panel;
	}
	
	private JPanel createControlPanel() {
		JPanel control_panel = new JPanel();
		control_panel.setLayout(new BoxLayout(control_panel, BoxLayout.Y_AXIS));
		
		Box title = uiFactory.createHeaderSection(CONTROL_PANEL_HEADER_TXT);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		control_panel.add(title);
		
		control_panel.add(uiFactory.createVerticalSpacing(CONTROL_VERTICAL_SPACING));
		
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		continueButton = uiFactory.createCenteredButton(PLAY_BUTTON_TXT);
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getXmlRpc().continue_robot();
			}
		});
		box.add(continueButton);
		
		box.add(uiFactory.createHorizontalSpacing());
		
		pauseButton = uiFactory.createCenteredButton(PAUSE_BUTTON_TXT);
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getXmlRpc().pause_robot();
			}
		});
		box.add(pauseButton);
		
		control_panel.add(box);
		
		return control_panel;
	}

	@Override
	public void openView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeView() {
		// TODO Auto-generated method stub
		
	}
	
	private MirXmlRpcAsyncCommunicator getXmlRpc() {
		return apiProvider.getApplicationAPI().getInstallationNode(MirInstallationNodeContribution.class).getXmlRpcHandle();
	}
	
	private Timer createTimer() {
		Timer timer = new Timer(true);
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (apiProvider.getApplicationAPI().getInstallationNode(MirInstallationNodeContribution.class).isDaemonRunning()) {
					try {
						String statusString = statusThread.getStatus();
						if (statusString.isEmpty()) {
							batteryLabel.setText(DEFAULT_VALUE_TXT);
							stateLabel.setText(DEFAULT_VALUE_TXT);
							missionLabel.setText(DEFAULT_VALUE_TXT);
							currentMissionLabel.setText(DEFAULT_VALUE_TXT);
							continueButton.setEnabled(false);
							pauseButton.setEnabled(false);
						} else {
							JsonObject status = new JsonParser().parse(statusString).getAsJsonObject();
							
							Double battery = status.get("battery_percentage").getAsDouble();
							DecimalFormat df = new DecimalFormat("#.##");
							batteryLabel.setText(df.format(battery).toString() + "%");
							
							String state = status.get("state_text").getAsString();
							stateLabel.setText(state);
							if (state.compareTo("Pause") != 0) {
								continueButton.setEnabled(false);
								pauseButton.setEnabled(true);
							} else {
								continueButton.setEnabled(true);
								pauseButton.setEnabled(false);
							}
							
							String mission = "<html>" + status.get("mission_text").getAsString() + "</html>";
							missionLabel.setText(mission);
							
							currentMissionLabel.setText(statusThread.getCurrentMission());
						}
					} catch (JsonParseException e) {
						System.out.println("The answer from the status endpoint is not a valid JSON");
						continueButton.setEnabled(true);
						pauseButton.setEnabled(true);
					}
				}
			}
		}, 1000, 1000);
		
		return timer;
	}

}
