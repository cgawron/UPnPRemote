package de.cgawron.upnp.tree;

import org.teleal.cling.model.meta.RemoteService;

class ServiceNode extends AbstractNode implements Node
{
	RemoteService service;

	public ServiceNode(DeviceNode parent, RemoteService service)
	{
		super(parent);
		this.service = service;

		initializeChildren();
	}

	protected void initializeChildren()
	{
		/*
		 * if
		 * (service.getServiceId().getId().equals(ContentTreeModel.CONTENT_DIRECTORY
		 * )) { children.add(new ContentDirectoryNode(this, service)); }
		 */
	}

	@Override
	public String toString()
	{
		return service.getServiceId().getId();
	}
}