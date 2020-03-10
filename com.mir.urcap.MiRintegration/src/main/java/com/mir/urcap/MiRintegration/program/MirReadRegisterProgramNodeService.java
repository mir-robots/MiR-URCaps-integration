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

public class MirReadRegisterProgramNodeService implements SwingProgramNodeService<MirReadRegisterProgramNodeContribution, MirReadRegisterProgramNodeView> {
	
	@Override
	public String getId() {
		return "MiR read register node";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Read MiR register";
	}

	@Override
	public MirReadRegisterProgramNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemApi = apiProvider.getSystemAPI();
		Style style = systemApi.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		
		return new MirReadRegisterProgramNodeView(style);
	}

	@Override
	public MirReadRegisterProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			MirReadRegisterProgramNodeView view, DataModel model, CreationContext context) {
		return new MirReadRegisterProgramNodeContribution(apiProvider, view, model);
	}

}
