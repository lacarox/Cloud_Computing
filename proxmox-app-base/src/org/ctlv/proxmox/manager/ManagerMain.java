package org.ctlv.proxmox.manager;

import org.ctlv.proxmox.api.ProxmoxAPI;

public class ManagerMain {

	public static void main(String[] args) throws Exception {
		
		ProxmoxAPI api = new ProxmoxAPI();
		Controller controller = new Controller(api);
		Analyzer analyzer = new Analyzer(api, controller);
		
		Monitor monitor = new Monitor(api, analyzer);
		
		monitor.run();
	}

}