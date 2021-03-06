package de.cgawron.upnp.tree;

import org.teleal.cling.model.meta.RemoteDevice;

@SuppressWarnings("rawtypes")
public class DeviceNode extends AbstractNode<RemoteDevice, GenericNode> implements Node<GenericNode>
{

	public DeviceNode(AbstractNode parent, RemoteDevice device)
	{
		super(parent, device);

		initializeChildren();
	}

	@Override
	public String toString()
	{
		return object.getDisplayString() + " [" + object.getType().getDisplayString() + "]";
	}

}