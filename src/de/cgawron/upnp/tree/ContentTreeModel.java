package de.cgawron.upnp.tree;

import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.header.ServiceTypeHeader;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DescMeta;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

public class ContentTreeModel extends AbstractUPnPTreeModel implements TreeModel, RegistryListener, Runnable
{
	UpnpService upnpService;

	public class MyBrowse extends Browse
	{
		ContentDirectoryNode node;

		@Override
		public String toString()
		{
			return "MyBrowse [node=" + node.name + "]";
		}

		public MyBrowse(ContentDirectoryNode node, BrowseFlag flag)
		{
			super(node.service, node.containerId, flag);
			this.node = node;
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
		public void updateStatus(Status status)
		{
			log.info("Browser: " + status);
		}

		@Override
		public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
		{
			log.severe("Broser failed: " + defaultMsg);
			node.initialized = false;
		}

	}

	static final Object CONTENT_DIRECTORY = "ContentDirectory";

	class ContentDirectoryNode extends AbstractLazyNode implements Node
	{

		String containerId;
		RemoteService service;
		String name = "retrieving content ...";

		boolean initialized = false;

		public ContentDirectoryNode(ServiceNode parent, RemoteService service)
		{
			super(parent);
			this.service = service;
			containerId = "0";
		}

		public ContentDirectoryNode(ContentDirectoryNode parent, String containerId)
		{
			super(parent);
			this.service = parent.service;
			this.containerId = containerId;
		}

		@Override
		public String toString()
		{
			if (!initialized)
				initialize();
			return "id: " + containerId + ", name=" + name;
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

		public void setName(String name)
		{
			this.name = name;
			TreeModelEvent ev = new TreeModelEvent(this, path);
			fireTreeNodesChanged(ev);
		}

		@Override
		void initializeChildren()
		{
			if (!initialized)
				initialize();
		}
	}

	Logger log = Logger.getLogger(ContentTreeModel.class.getName());

	RootNode root = new RootNode(this);

	public ContentTreeModel(UpnpService upnpService)
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

	@Override
	public Node getRoot()
	{
		return root;
	}

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
			ServiceType serviceType = ServiceType.valueOf("urn:schemas-upnp-org:service:ContentDirectory:1");
			upnpService.getControlPoint().search(new ServiceTypeHeader(serviceType));
		} catch (Exception ex) {
			System.err.println("Exception occured: " + ex);
			System.exit(1);
		}
	}
}