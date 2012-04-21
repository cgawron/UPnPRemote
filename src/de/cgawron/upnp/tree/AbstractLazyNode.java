package de.cgawron.upnp.tree;

import java.util.Collection;

/**
 * A {@code Node} with lazy initialization of children.
 * 
 * @author Christian Gawron
 * 
 * @param <ChildT>
 */
abstract class AbstractLazyNode<T, ChildT extends Node<?>> extends AbstractNode<T, ChildT>
{
	@SuppressWarnings("rawtypes")
	AbstractLazyNode(AbstractNode parent)
	{
		super(parent);
	}

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