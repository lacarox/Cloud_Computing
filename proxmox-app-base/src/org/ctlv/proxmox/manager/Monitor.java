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

public class Monitor implements Runnable {

	Analyzer analyzer;
	ProxmoxAPI api;
	
	public Monitor(ProxmoxAPI api, Analyzer analyzer) {
		this.api = api;
		this.analyzer = analyzer;
	}
	

	@Override
	public void run() {
		
		while(true) {
			
				
			
			Map<String, List<LXC>> mapToAnalyze = new HashMap<String, List<LXC>>();
			try {
				// R�cup�rer les donn�es sur les serveurs		
				mapToAnalyze.put(Constants.SERVER1, api.getOurCTs(Constants.SERVER1));
				mapToAnalyze.put(Constants.SERVER2, api.getOurCTs(Constants.SERVER2));
				
				// Lancer l'analyse
				analyzer.analyze(mapToAnalyze);
				
			} catch (LoginException | JSONException | IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			

			
			// attendre une certaine p�riode
			try {
				Thread.sleep(Constants.MONITOR_PERIOD * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
