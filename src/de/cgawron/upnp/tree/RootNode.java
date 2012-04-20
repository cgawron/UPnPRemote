package de.cgawron.upnp.tree;

import java.util.HashMap;
import java.util.Map;

import org.teleal.cling.model.meta.DeviceIdentity;

class RootNode extends AbstractNode<Void, DeviceNode> implements Node<DeviceNode>
{
	Map<DeviceIdentity, DeviceNode> topLevelNodes = new HashMap<DeviceIdentity, DeviceNode>();

	RootNode(AbstractUPnPTreeModel treeModel)
	{
		super(treeModel);
	}

	@Override
	public void addChild(DeviceNode child)
	{
		topLevelNodes.put(child.object.getIdentity(), child);
		super.addChild(child);
	}

	@Override
	public String toString()
	{
		return "RootNode []";
	}

}