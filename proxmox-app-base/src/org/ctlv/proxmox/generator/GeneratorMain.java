package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class GeneratorMain {
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
	
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;
		
		
		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime()); 
		
		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
		
		while (true) {
			
			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			long memOnServer1 = 0;
			List<LXC> listLXC = api.getCTs(Constants.SERVER1);
			for (LXC CT : listLXC) {
				memOnServer1 += CT.getMem();
			}
			
			long memOnServer2 = 0;
			listLXC = api.getCTs(Constants.SERVER2);
			for (LXC CT : listLXC) {
				memOnServer2 += CT.getMem();
			}
			
			// M�moire autoris�e sur chaque serveur
			float memRatioOnServer1 = (float) (api.getNode(Constants.SERVER1).getMemory_total()*0.16);
			// ...
			float memRatioOnServer2 = (float) (api.getNode(Constants.SERVER2).getMemory_total()*0.16);
			// ... 
			
			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) {  // Exemple de condition de l'arr�t de la g�n�ration de CTs
				
				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;
				
				// cr�er un contenaire sur ce serveur
				api.createCT(serverName, Constants.CT_BASE_ID+"", Constants.CT_BASE_NAME, Constants.RAM_SIZE[1]);
				
				while(!api.getCT(serverName, Constants.CT_BASE_ID+"").getStatus().equals("running")){
					try {
						api.startCT(serverName, Constants.CT_BASE_ID+"");
					}catch(Exception e) {
						
					}
					
					Thread.sleep(1000);
				}
				System.out.println(Constants.CT_BASE_ID+" started !");
				
				Constants.CT_BASE_ID++;
				// planifier la prochaine cr�ation				
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);
				
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
		}
		
	}

}
