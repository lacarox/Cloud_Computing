package TP2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;


public class FirstPart {

	public static void main(String[] args) throws LoginException, JSONException, IOException {
		// TODO Auto-generated method stub
			ProxmoxAPI api = new ProxmoxAPI();
			
			Node node = api.getNode(Constants.SERVER1);
			
			System.out.println("Host Name : "+Constants.SERVER1);
			System.out.println("CPU Usage : "+node.getCpu()*100+" %");
			System.out.println("Disk Usage : "+(float)node.getRootfs_used()/(float)node.getRootfs_total()*100+" %");
			
			System.out.println("----------------------");
			//api.deleteCT(Constants.SERVER1, "3300");
			
			if(api.getCT(Constants.SERVER1, "195").equals(null)){
				api.createCT(Constants.SERVER1, "195", Constants.CT_BASE_NAME+"2", Constants.RAM_SIZE[1]);
			}		
			
			while(!api.getCT(Constants.SERVER1, "195").getStatus().equals("running")){
				api.startCT(Constants.SERVER1, "195");
			}
				
			
			List<LXC> listCT = api.getCTs(Constants.SERVER1);			
			for (LXC CT : listCT) {
			
				//if(CT.getStatus().equals("running")) {
					System.out.println("Name : "+CT.getName());
					System.out.println("ID : "+CT.getVmid());
					System.out.println("State : "+CT.getStatus());
					System.out.println("CPU Usage : "+CT.getCpu());
					System.out.println("Disk Usage : "+(CT.getDisk()/Math.pow(1024,2))+ " MB");
					System.out.println("Memory Usage : "+(CT.getMem()/Math.pow(1024,2))+ " MB\n");
				//}
			}
			
	}

}
