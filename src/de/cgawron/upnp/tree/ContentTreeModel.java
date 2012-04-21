package de.cgawron.upnp.tree;

import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.header.ServiceTypeHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DescMeta;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

public class ContentTreeModel extends AbstractUPnPTreeModel implements Runnable
{
	class ContentDirectoryNode extends AbstractLazyNode implements Node
	{

		String containerId;
		RemoteService service;
		String name = "retrieving content ...";

		boolean initialized = false;

		public ContentDirectoryNode(ContentDirectoryNode parent, String containerId)
		{
			super(parent);
			this.service = parent.service;
			this.containerId = containerId;
		}

		public ContentDirectoryNode(ServiceNode parent, RemoteService service)
		{
			super(parent);
			this.service = service;
			containerId = "0";
		}

		private synchronized void initialize()
		{
			Browse browse = new MyBrowse(this, BrowseFlag.METADATA);
			log.info("executing " + browse);
			upnpService.getControlPoint().execute(browse);
			Browse browseChildren = new MyBrowse(this, BrowseFlag.DIRECT_CHILDREN);
			log.info("executing " + browseChildren);
			upnpService.getControlPoint().execute(browseChildren);

			initialized = true;
		}

		@Override
		void initializeChildren()
		{
			if (!initialized)
				initialize();
		}

		public void setName(String name)
		{
			this.name = name;
			TreeModelEvent ev = new TreeModelEvent(this, path);
			fireTreeNodesChanged(ev);
		}

		@Override
		public String toString()
		{
			if (!initialized)
				initialize();
			return "id: " + containerId + ", name=" + name;
		}
	}
	public class MyBrowse extends Browse
	{
		ContentDirectoryNode node;

		public MyBrowse(ContentDirectoryNode node, BrowseFlag flag)
		{
			super(node.service, node.containerId, flag);
			this.node = node;
		}

		@Override
		public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
		{
			log.severe("Broser failed: " + defaultMsg);
			node.initialized = false;
		}

		@Override
		public void received(ActionInvocation actionInvocation, DIDLContent content)
		{
			for (Item item : content.getItems()) {
				log.info("item " + item + ": " + item.getTitle());
			}
			for (DescMeta meta : content.getDescMetadata()) {
				log.info("meta " + meta);
			}
			for (Container c : content.getContainers()) {
				log.info("container " + c + ": " + c.getTitle());
				if (c.getId().equals(node.containerId)) {
					node.setName(c.getTitle());
				}
				else {
					node.addChild(new ContentDirectoryNode(node, c.getId()));
				}
			}

		}

		@Override
		public String toString()
		{
			return "MyBrowse [node=" + node.name + "]";
		}

		@Override
		public void updateStatus(Status status)
		{
			log.info("Browser: " + status);
		}

	}

	Logger log = Logger.getLogger(ContentTreeModel.class.getName());

	final DeviceType MEDIA_SERVER = DeviceType.valueOf("urn:schemas-upnp-org:device:MediaServer:1");

	UpnpService upnpService;

	static final Object CONTENT_DIRECTORY = "ContentDirectory";

	public ContentTreeModel()
	{
		super();
		root = new DeviceTypeNode(this, MEDIA_SERVER);
	}

	public ContentTreeModel(UpnpService upnpService)
	{
		super();
		this.upnpService = upnpService;
		root = new DeviceTypeNode(this, MEDIA_SERVER);

		Thread clientThread = new Thread(this);
		clientThread.setDaemon(false);
		clientThread.start();
	}

	@Override
	void initializeChildren(AbstractNode<?, ? extends Node<?>> node)
	{
		if (node instanceof ServiceNode) {
			ServiceNode s = (ServiceNode) node;
			if (s.object.getServiceId().getId().equals(ContentTreeModel.CONTENT_DIRECTORY)) {
				s.children.add(new ContentDirectoryNode(s, s.object));
			}
		}
		else
			super.initializeChildren(node);
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device)
	{
		log.info("Remote device available: " + device.getDisplayString());
		log.info("Device type: " + device.getType());
		log.info("Device details: " + device.getIdentity().toString());

		if (device.getType().equals(MEDIA_SERVER)) {
			root.addChild(new DeviceNode(root, device));
		}
	}

	@Override
	public void run()
	{
		try {

			// Add a listener for device registration events
			upnpService.getRegistry().addListener(this);

			// Broadcast a search message for all devices
			ServiceType serviceType = ServiceType.valueOf("urn:schemas-upnp-org:service:ContentDirectory:1");
			upnpService.getControlPoint().search(new ServiceTypeHeader(serviceType));
		} catch (Exception ex) {
			System.err.println("Exception occured: " + ex);
			System.exit(1);
		}
	}
}