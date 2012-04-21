package de.cgawron.upnp.tree;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.StateVariable;

public class DeviceTreeModel extends AbstractUPnPTreeModel implements Runnable
{
	UpnpService upnpService;

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

	public DeviceTreeModel(UpnpService upnpService)
	{
		this.upnpService = upnpService;

		Thread clientThread = new Thread(this);
		clientThread.setDaemon(false);
		clientThread.start();
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

	@Override
	void initializeChildren(AbstractNode<?, ? extends Node<?>> node)
	{
		if (node instanceof ServiceNode) {
			ServiceNode s = (ServiceNode) node;
			for (StateVariable<RemoteService> variable : s.object.getStateVariables()) {
				s.addChild(new VariableNode(s, variable));
			}

			for (Action<RemoteService> action : s.object.getActions()) {
				s.addChild(new ActionNode(s, action));
			}
		}
		if (node instanceof DeviceNode) {
			DeviceNode dn = (DeviceNode) node;
			RemoteDevice device = dn.object;

			for (RemoteDevice embedded : device.getEmbeddedDevices()) {
				dn.addChild(new DeviceNode(dn, embedded));
			}
		}
		super.initializeChildren(node);
	}

}