package de.cgawron.upnp.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

public class DeviceTreeModel extends AbstractUPnPTreeModel implements TreeModel, RegistryListener, Runnable
{
	UpnpService upnpService;

	@SuppressWarnings("rawtypes")
	class MyServiceNode extends ServiceNode
	{
		public MyServiceNode(DeviceNode parent, RemoteService service)
		{
			super(parent, service);
		}

		@Override
		protected void initializeChildren()
		{
			for (StateVariable<RemoteService> variable : service.getStateVariables()) {
				addChild(new VariableNode(this, variable));
			}

			for (Action<RemoteService> action : service.getActions()) {
				addChild(new ActionNode(this, action));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	class ActionNode extends AbstractNode<Action, Node> implements Node<Node>
	{
		public ActionNode(ServiceNode parent, Action action)
		{
			super(parent, action);
		}

		@Override
		public String toString()
		{
			return object.getName();
		}

		@Override
		void initializeChildren()
		{
			// TODO Auto-generated method stub

		}
	}

	@SuppressWarnings("rawtypes")
	class VariableNode extends AbstractNode<StateVariable, Node> implements Node<Node>
	{
		public VariableNode(ServiceNode parent, StateVariable variable)
		{
			super(parent, variable);
		}

		@Override
		public String toString()
		{
			return object.getName();
		}

		@Override
		void initializeChildren()
		{
			// TODO Auto-generated method stub

		}
	}

	RootNode root = new RootNode(this);

	public DeviceTreeModel(UpnpService upnpService)
	{
		this.upnpService = upnpService;

		Thread clientThread = new Thread(this);
		clientThread.setDaemon(false);
		clientThread.start();
	}

	@Override
	public void afterShutdown()
	{
		System.out.println("Shutdown of registry complete!");

	}

	@Override
	public void beforeShutdown(Registry registry)
	{
		System.out.println("Before shutdown, the registry has devices: " + registry.getDevices().size());
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
	public Node getRoot()
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
	public void localDeviceAdded(Registry registry, LocalDevice device)
	{
		System.out.println("Local device added: " + device.getDisplayString());
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device)
	{
		System.out.println("Local device removed: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device)
	{
		System.out.println("Remote device available: " + device.getDisplayString());
		System.out.println("Device details: " + device.getIdentity().toString());

		root.addChild(new DeviceNode(root, device));
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex)
	{
		System.out.println("Discovery failed: " + device.getDisplayString() + " => " + ex);
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
	{
		System.out.println("Discovery started: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
	{
		System.out.println("Remote device removed: " + device.getDisplayString());
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device)
	{
		System.out.println("Remote device updated: " + device.getDisplayString());
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void run()
	{
		try {

			// Add a listener for device registration events
			upnpService.getRegistry().addListener(this);

			// Broadcast a search message for all devices
			upnpService.getControlPoint().search(new STAllHeader());
		} catch (Exception ex) {
			System.err.println("Exception occured: " + ex);
			System.exit(1);
		}
	}
}