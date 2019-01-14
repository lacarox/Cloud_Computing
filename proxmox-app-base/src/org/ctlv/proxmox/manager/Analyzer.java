package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;
	
	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}
	
	public void analyze(Map<String, List<LXC>> myCTsPerServer) throws InterruptedException, LoginException, JSONException, IOException {

		// Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
		
		Map<String, Long> serverRAM = new HashMap<String, Long>();		
		Map<String, Float> serverRAMRatio8 = new HashMap<String, Float>();
		Map<String, Float> serverRAMRatio12 = new HashMap<String, Float>();
		
		for (Map.Entry<String, List<LXC>> entry : myCTsPerServer.entrySet()) {
			String serverName = entry.getKey();
			List<LXC> listLXC = entry.getValue();
			
			long memOnServer = 0;
			for (LXC CT : listLXC) {
				memOnServer += CT.getMem(); 
			}
			
			// M�moire autoris�e sur chaque serveur
			float memRatioOnServer8 = (float) (api.getNode(serverName).getMemory_total()*Constants.MIGRATION_THRESHOLD);
			float memRatioOnServer12 = (float) (api.getNode(serverName).getMemory_total()*Constants.DROPPING_THRESHOLD);
			
			serverRAM.put(serverName, memOnServer);
			serverRAMRatio8.put(serverName, memRatioOnServer8);
			serverRAMRatio12.put(serverName, memRatioOnServer12);
			
			// Analyse et Actions
			//12% stop the oldest
			if (memOnServer > memRatioOnServer12) {
				controller.offLoad(serverName);
			}
			
		}

	
		
		//8% load balancing
		if (serverRAM.get(Constants.SERVER1) > serverRAMRatio8.get(Constants.SERVER1) && serverRAM.get(Constants.SERVER2) < serverRAMRatio12.get(Constants.SERVER2)) {
			controller.migrateFromTo(Constants.SERVER1,Constants.SERVER2);
		}
		if (serverRAM.get(Constants.SERVER1) < serverRAMRatio8.get(Constants.SERVER1) && serverRAM.get(Constants.SERVER2) > serverRAMRatio12.get(Constants.SERVER2)) {
			controller.migrateFromTo(Constants.SERVER2,Constants.SERVER1);
		}
		
		
	}

}
