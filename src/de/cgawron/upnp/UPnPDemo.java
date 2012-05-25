package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.registry.RegistryListener;

import de.cgawron.upnp.gui.ContentPanel;
import de.cgawron.upnp.gui.ControlPanel;
import de.cgawron.upnp.gui.RendererPanel;
import de.cgawron.upnp.tree.ContentTreeModel;
import de.cgawron.upnp.tree.DeviceTreeModel;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class UPnPDemo
{
	RegistryListener listener;
	static UpnpService upnpService;

	private static ContentPanel contentPanel;
	private static RendererPanel rendererPanel;
	private static JPanel controlPanel;

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
		DeviceTreeModel deviceModel = new DeviceTreeModel(upnpService);
		contentPanel = new ContentPanel(contentModel);
		rendererPanel = new RendererPanel(deviceModel);
		rendererPanel.setPreferredSize(new Dimension(400, 400));
		controlPanel = new ControlPanel(upnpService, deviceModel, contentModel);
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, rendererPanel);
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, controlPanel);
		main.getContentPane().add(splitPane2, BorderLayout.CENTER);
		main.pack();
		main.setVisible(true);

	}
}
