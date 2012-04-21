package de.cgawron.upnp.tree;

import org.teleal.cling.model.meta.RemoteService;

class ServiceNode extends AbstractNode<RemoteService, AbstractNode> implements Node<AbstractNode>
{

	public ServiceNode(DeviceNode parent, RemoteService service)
	{
		super(parent);
		this.object = service;

		initializeChildren();
	}

	@Override
	public String toString()
	{
		return object.getServiceId().getId();
	}
}