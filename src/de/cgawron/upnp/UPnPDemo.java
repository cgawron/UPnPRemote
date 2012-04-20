package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.registry.RegistryListener;

import de.cgawron.upnp.tree.ContentTreeModel;
import de.cgawron.upnp.tree.DeviceTreeModel;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class UPnPDemo
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
		rendererPanel = new RendererPanel(new DeviceTreeModel(upnpService));
		rendererPanel.setPreferredSize(new Dimension(400, 400));
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, rendererPanel);
		main.getContentPane().add(splitPane, BorderLayout.CENTER);
		main.pack();
		main.setVisible(true);

	}
}
