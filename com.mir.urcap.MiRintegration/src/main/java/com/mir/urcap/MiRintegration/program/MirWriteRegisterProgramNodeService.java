package com.mir.urcap.MiRintegration.program;

import java.util.Locale;

import com.mir.urcap.MiRintegration.style.Style;
import com.mir.urcap.MiRintegration.style.V3Style;
import com.mir.urcap.MiRintegration.style.V5Style;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

public class MirWriteRegisterProgramNodeService implements 
	SwingProgramNodeService<MirWriteRegisterProgramNodeContribution, MirWriteRegisterProgramNodeView> {

	@Override
	public String getId() {
		return "MiR write register node";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Write MiR register";
	}

	@Override
	public MirWriteRegisterProgramNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		
		return new MirWriteRegisterProgramNodeView(style);
	}

	@Override
	public MirWriteRegisterProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			MirWriteRegisterProgramNodeView view, DataModel model, CreationContext context) {
		return new MirWriteRegisterProgramNodeContribution(apiProvider, view, model);
	}

}
