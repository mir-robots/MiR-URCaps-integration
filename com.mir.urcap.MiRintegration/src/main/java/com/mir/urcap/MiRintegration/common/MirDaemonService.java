package com.mir.urcap.MiRintegration.common;

import java.net.MalformedURLException;
import java.net.URL;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.DaemonService;

public class MirDaemonService implements DaemonService {

	private DaemonContribution daemonContribution;
	
	@Override
	public void init(DaemonContribution daemon) {
		this.daemonContribution = daemon;
		try {
			daemon.installResource(new URL("file:daemon/mirRest/"));
		} catch (MalformedURLException e) { }
	}

	@Override
	public URL getExecutable() {
		try {
			return new URL("file:daemon/mirRest/mirRest.py");
		} catch (MalformedURLException e) { 
			System.out.println("Error loading daemon, returning null");
			return null;
		}
	}
	
	public DaemonContribution getDaemonContribution() {
		return this.daemonContribution;
	}

}

