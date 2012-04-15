package de.cgawron.upnp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DescMeta;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

class ServerTreeModel extends TreeModelSupport implements TreeModel, RegistryListener
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

	private static final Object CONTENT_DIRECTORY = "ContentDirectory";

	interface Node<ChildT extends Node<?>>
	{
		public void addChild(ChildT node);

		public ChildT getChild(int index);

		public int getChildCount();

		Collection<ChildT> getChildren();

		public boolean isLeaf();
	}

	class AbstractNode<ChildT extends Node<?>> implements Node<ChildT>
	{
		protected Vector<ChildT> children = new Vector<ChildT>();
		protected TreePath path;

		AbstractNode()
		{
			path = new TreePath(this);
		}

		AbstractNode(AbstractNode parent)
		{
			path = parent.path.pathByAddingChild(this);
		}

		@Override
		public void addChild(ChildT child)
		{
			log.info("adding child " + child);
			children.add(child);
			int[] indices = new int[1];
			Node[] newChildren = new Node[1];
			newChildren[0] = child;
			indices[0] = children.indexOf(child);
			TreeModelEvent ev = new TreeModelEvent(this, path, indices, newChildren);
			fireTreeNodesInserted(ev);
		}

		@Override
		public ChildT getChild(int index)
		{
			return children.get(index);
		}

		@Override
		public int getChildCount()
		{
			return children.size();
		}

		@Override
		public Collection<ChildT> getChildren()
		{
			return children;
		}

		@Override
		public boolean isLeaf()
		{
			log.info("isLeaf " + this + ": " + children);
			return children.size() == 0;
		}

	}

	/**
	 * A {@code Node} with lazy initialization of children.
	 * 
	 * @author Christian Gawron
	 * 
	 * @param <ChildT>
	 */
	abstract class AbstractLazyNode<ChildT extends Node<?>> extends AbstractNode<ChildT>
	{
		AbstractLazyNode(AbstractNode parent)
		{
			super(parent);
		}

		abstract void initializeChildren();

		@Override
		public ChildT getChild(int index)
		{
			initializeChildren();
			return super.getChild(index);
		}

		@Override
		public int getChildCount()
		{
			initializeChildren();
			return super.getChildCount();
		}

		@Override
		public Collection<ChildT> getChildren()
		{
			initializeChildren();
			return super.getChildren();
		}

		@Override
		public boolean isLeaf()
		{
			initializeChildren();
			return super.isLeaf();
		}

	}

	class DeviceNode extends AbstractNode<ServiceNode> implements Node<ServiceNode>
	{
		RemoteDevice device;

		public DeviceNode(RootNode parent, RemoteDevice device)
		{
			super(parent);
			this.device = device;

			RemoteService[] services = device.getServices();
			for (RemoteService service : services) {
				children.add(new ServiceNode(this, service));
			}
		}

		@Override
		public String toString()
		{
			return device.getDisplayString();
		}

	}

	class RootNode extends AbstractNode<DeviceNode> implements Node<DeviceNode>
	{
		Map<DeviceIdentity, DeviceNode> topLevelNodes = new HashMap<DeviceIdentity, DeviceNode>();

		RootNode()
		{
		}

		@Override
		public void addChild(DeviceNode child)
		{
			topLevelNodes.put(child.device.getIdentity(), child);
			super.addChild(child);
		}

		@Override
		public String toString()
		{
			return "RootNode []";
		}

	}

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

	class ServiceNode extends AbstractNode implements Node
	{
		RemoteService service;

		public ServiceNode(DeviceNode parent, RemoteService service)
		{
			super(parent);
			this.service = service;

			if (service.getServiceId().getId().equals(CONTENT_DIRECTORY))
			{
				children.add(new ContentDirectoryNode(this, service));
			}
		}

		@Override
		public String toString()
		{
			return service.getServiceId().getId();
		}
	}

	Logger log = Logger.getLogger(ServerTreeModel.class.getName());

	RootNode root = new RootNode();

	ServerTreeModel(UpnpService upnpService)
	{
		this.upnpService = upnpService;
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

}