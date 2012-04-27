package de.cgawron.upnp.tree;

class GenericNode<ChildT extends Node<?>> extends AbstractNode<Void, ChildT>
{
	private String name;

	GenericNode(AbstractUPnPTreeModel treeModel)
	{
		super(treeModel);
	}

	GenericNode(AbstractNode<?, ?> parent, String name)
	{
		super(parent);
		this.name = name;
	}

	@Override
	public void addChild(ChildT child)
	{
		super.addChild(child);
	}

	@Override
	public String toString()
	{
		return name;
	}
}