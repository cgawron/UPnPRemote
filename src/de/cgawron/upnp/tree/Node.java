package de.cgawron.upnp.tree;

import java.util.Collection;

interface Node<ChildT extends Node<?>>
{
	public void addChild(ChildT node);

	public ChildT getChild(int index);

	public int getChildCount();

	Collection<ChildT> getChildren();

	public boolean isLeaf();
}