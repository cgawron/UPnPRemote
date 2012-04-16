package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

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
	private static JSplitPane splitPane;
	private static ContentPanel contentPanel;
	private static RendererPanel rendererPanel;

	public UPnPDemo(RegistryListener listener)
	{
		this.listener = listener;
	}

	public static void main(String[] args) throws Exception
	{
		upnpService = new UpnpServiceImpl();
		JFrame main = new JFrame("UPnP Demo");
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ContentTreeModel contentModel = new ContentTreeModel(upnpService);
		contentPanel = new ContentPanel(contentModel);
		rendererPanel = new RendererPanel();
		rendererPanel.setPreferredSize(new Dimension(400, 400));
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, rendererPanel);
		main.getContentPane().add(splitPane, BorderLayout.CENTER);
		main.pack();
		main.setVisible(true);

		Thread clientThread = new Thread(new UPnPDemo(contentModel));
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
