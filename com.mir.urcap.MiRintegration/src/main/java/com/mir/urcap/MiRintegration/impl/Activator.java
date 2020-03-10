package com.mir.urcap.MiRintegration.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mir.urcap.MiRintegration.common.MirDaemonService;
import com.mir.urcap.MiRintegration.installationnode.MirInstallationNodeService;
import com.mir.urcap.MiRintegration.program.MirReadRegisterProgramNodeService;
import com.mir.urcap.MiRintegration.program.MirWriteRegisterProgramNodeService;
import com.mir.urcap.MiRintegration.toolbar.MirToolbarService;
import com.ur.urcap.api.contribution.DaemonService;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.contribution.toolbar.swing.SwingToolbarService;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Starting MiR Integration with UR Cap");
		
		MirDaemonService daemonService = new MirDaemonService();
		bundleContext.registerService(DaemonService.class, daemonService, null);
		
		bundleContext.registerService(SwingInstallationNodeService.class, new MirInstallationNodeService(daemonService), null);
		bundleContext.registerService(SwingToolbarService.class, new MirToolbarService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new MirWriteRegisterProgramNodeService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new MirReadRegisterProgramNodeService(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		System.out.println("Stopping MiR Integration with UR Cap");
	}
}

