package de.cgawron.upnp.tree;

import java.util.HashMap;
import java.util.Map;

import org.teleal.cling.model.types.DeviceType;

public class RootNode extends AbstractNode<Void, DeviceTypeNode> implements Node<DeviceTypeNode>
{
	Map<DeviceType, DeviceTypeNode> topLevelNodes = new HashMap<DeviceType, DeviceTypeNode>();

	RootNode(AbstractUPnPTreeModel treeModel)
	{
		super(treeModel);
	}

	@Override
	public void addChild(DeviceTypeNode child)
	{
		topLevelNodes.put(child.object, child);
		super.addChild(child);
	}

	@Override
	public String toString()
	{
		return "RootNode []";
	}

	public DeviceTypeNode getChild(DeviceType type)
	{
		DeviceTypeNode node;
		if (topLevelNodes.containsKey(type)) {
			node = topLevelNodes.get(type);
		}
		else {
			node = new DeviceTypeNode(this, type);
			addChild(node);
		}
		return node;
	}

}