package de.cgawron.upnp.tree;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

public class AbstractUPnPTreeModel implements TreeModel, RegistryListener
{
	UpnpService upnpService;

	Logger log = Logger.getLogger(AbstractUPnPTreeModel.class.getName());

	private final Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

	@SuppressWarnings("rawtypes")
	protected AbstractNode root;

	public AbstractUPnPTreeModel(UpnpService upnpService)
	{
		this.upnpService = upnpService;
		root = new RootNode(this);
	}

	void initializeChildren(AbstractNode<?, ? extends Node<?>> node)
	{
		if (node.object.getClass() == Void.class) {
			// Do nothing
		}
		else if (node instanceof DeviceNode) {
			DeviceNode dn = (DeviceNode) node;
			RemoteDevice device = dn.object;

			@SuppressWarnings("rawtypes")
			GenericNode<GenericNode> attributesRoot = new GenericNode<GenericNode>(node, "Details");
			GenericNode<DeviceNode> childDevicesRoot = new GenericNode<DeviceNode>(node, "Devices");
			GenericNode<ServiceNode> servicesRoot = new GenericNode<ServiceNode>(node, "Services");
			dn.children.add(attributesRoot);
			dn.children.add(childDevicesRoot);
			dn.children.add(servicesRoot);

			attributesRoot.addChild(new GenericNode(attributesRoot, device.getIdentity().toString()));
			attributesRoot.addChild(new GenericNode(attributesRoot, device.getDetails().getFriendlyName()));
			attributesRoot.addChild(new GenericNode(attributesRoot, device.getDetails().getSerialNumber()));

			RemoteService[] services = dn.object.getServices();
			for (RemoteService service : services) {
				servicesRoot.children.add(new ServiceNode(dn, service));
			}
			for (RemoteDevice embedded : device.getEmbeddedDevices()) {
				childDevicesRoot.addChild(new DeviceNode(dn, embedded));
			}
		}
		else if (node instanceof ServiceNode) {
			final ServiceNode s = (ServiceNode) node;
			final GenericNode<GenericNode> details = new GenericNode<GenericNode>(node, "Details");
			s.children.add(details);

			for (Action<RemoteService> action : s.object.getActions())
			{
				details.children.add(new GenericNode(s, action.toString()));
			}

			if (s.object.getServiceId().getId().equals("ConnectionManager"))
			{

				Action<RemoteService> protocolInfo = s.object.getAction("GetProtocolInfo");
				ActionInvocation<RemoteService> invocation = new ActionInvocation<RemoteService>(protocolInfo);
				ActionCallback callback = new ActionCallback(invocation) {

					@Override
					public void success(ActionInvocation invocation)
					{
						// TODO
						log.info(invocation.toString());
						details.children.add(new GenericNode(s, invocation.getOutput()[0].toString()));
						details.children.add(new GenericNode(s, invocation.getOutput()[1].toString()));
					}

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
					{
						log.log(Level.WARNING, "error in callback", invocation.getFailure());
						details.children.add(new GenericNode(s, "error calling GetProtocolOnfo"));
					}
				};
				upnpService.getControlPoint().execute(callback);
				// details.children.add();
			}

		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener)
	{
		if (listener != null && !listeners.contains(listener)) {
			listeners.addElement(listener);
		}
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener)
	{
		if (listener != null) {
			listeners.removeElement(listener);
		}
	}

	public void fireTreeNodesChanged(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeStructureChanged(e);
		}
	}

	@Override
	public void afterShutdown()
	{
		log.info("Shutdown of registry complete!");

	}

	@Override
	public void beforeShutdown(Registry registry)
	{
		log.info("Before shutdown, the registry has devices: " + registry.getDevices().size());
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device)
	{
		log.info("Local device added: " + device.getDisplayString());
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device)
	{
		log.info("Local device removed: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device)
	{
		log.info("Remote device available: " + device.getDisplayString());
		log.info("Device details: " + device.getIdentity().toString());

		((RootNode) root).getChild(device.getType()).addChild(new DeviceNode(root, device));
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex)
	{
		log.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
	{
		log.info("Discovery started: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
	{
		log.info("Remote device removed: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device)
	{
		log.info("Remote device updated: " + device.getDisplayString());
	}

	@Override
	public Node<?> getChild(Object _parent, int index)
	{
		Node<?> parent = (Node<?>) _parent;
		return parent.getChild(index);
	}

	@Override
	public int getChildCount(Object _parent)
	{
		Node<?> parent = (Node<?>) _parent;
		return parent.getChildCount();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AbstractNode getRoot()
	{
		return root;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isLeaf(Object _node)
	{
		Node node = (Node) _node;

		return node.isLeaf();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// TODO Auto-generated method stub

	}
}
