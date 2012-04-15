package de.cgawron.upnp;

import javax.swing.JFrame;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.message.header.ServiceTypeHeader;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.RegistryListener;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class UPnPDemo implements Runnable
{
	RegistryListener listener;
	static UpnpService upnpService;

	public UPnPDemo(RegistryListener listener)
	{
		this.listener = listener;
	}

	public static void main(String[] args) throws Exception
	{
		upnpService = new UpnpServiceImpl();
		JFrame main = new JFrame("UPnP Demo");
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ServerTreeModel model = new ServerTreeModel(upnpService);
		ServerPanel serverPanel = new ServerPanel(model);
		main.getContentPane().add(serverPanel);
		main.pack();
		main.setVisible(true);

		// Start a user thread that runs the UPnP stack
		Thread clientThread = new Thread(new UPnPDemo(serverPanel.model));
		clientThread.setDaemon(false);
		clientThread.start();

	}

	@Override
	public void run()
	{
		try {

			// Add a listener for device registration events
			if (listener != null)
				upnpService.getRegistry().addListener(listener);

			// Broadcast a search message for all devices
			ServiceType serviceType = ServiceType.valueOf("urn:schemas-upnp-org:service:ContentDirectory:1");
			upnpService.getControlPoint().search(new ServiceTypeHeader(serviceType));
		} catch (Exception ex) {
			System.err.println("Exception occured: " + ex);
			System.exit(1);
		}
	}
}
