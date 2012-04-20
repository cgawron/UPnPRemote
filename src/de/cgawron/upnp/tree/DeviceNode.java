package de.cgawron.upnp.tree;

import org.teleal.cling.model.meta.RemoteDevice;

class DeviceNode extends AbstractNode<RemoteDevice, ServiceNode> implements Node<ServiceNode>
{

	public DeviceNode(RootNode parent, RemoteDevice device)
	{
		super(parent, device);

		initializeChildren();
	}

	@Override
	public String toString()
	{
		return object.getDisplayString();
	}

}