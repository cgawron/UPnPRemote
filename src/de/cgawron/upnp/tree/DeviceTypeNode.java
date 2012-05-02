package de.cgawron.upnp.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.types.DeviceType;

@SuppressWarnings("rawtypes")
public class DeviceTypeNode extends AbstractNode<DeviceType, DeviceNode> implements Node<DeviceNode>
{
	Logger log = Logger.getLogger(DeviceTypeNode.class.getName());

	Map<DeviceIdentity, DeviceNode> topLevelNodes = new HashMap<DeviceIdentity, DeviceNode>();

	public DeviceTypeNode(AbstractUPnPTreeModel treeModel, DeviceType deviceType)
	{
		super(treeModel, deviceType);
	}

	public DeviceTypeNode(AbstractNode parent, DeviceType deviceType)
	{
		super(parent, deviceType);

		initializeChildren();
	}

	@Override
	public void addChild(DeviceNode child)
	{
		if (topLevelNodes.containsKey(child.object.getIdentity())) {
			log.warning("node " + child + " already exists");
		}
		else {
			topLevelNodes.put(child.object.getIdentity(), child);
			super.addChild(child);
		}
	}

	@Override
	public String toString()
	{
		return object.getDisplayString();
	}

}