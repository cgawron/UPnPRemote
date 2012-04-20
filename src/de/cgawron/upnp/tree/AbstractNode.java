package de.cgawron.upnp.tree;

import java.util.Collection;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

class AbstractNode<T, ChildT extends Node<?>> implements Node<ChildT>
{
	protected final AbstractUPnPTreeModel treeModel;
	protected T object;
	protected Vector<ChildT> children = new Vector<ChildT>();
	protected TreePath path;

	AbstractNode(AbstractUPnPTreeModel treeModel)
	{
		this.treeModel = treeModel;
		path = new TreePath(this);
	}

	AbstractNode(AbstractNode parent)
	{
		this.treeModel = parent.treeModel;
		path = parent.path.pathByAddingChild(this);
	}

	AbstractNode(AbstractNode parent, T object)
	{
		this.treeModel = parent.treeModel;
		this.object = object;
		path = parent.path.pathByAddingChild(this);
	}

	void initializeChildren()
	{
		treeModel.initializeChildren(this);
	}

	@Override
	public void addChild(ChildT child)
	{
		this.treeModel.log.info("adding child " + child);
		children.add(child);
		int[] indices = new int[1];
		Node[] newChildren = new Node[1];
		newChildren[0] = child;
		indices[0] = children.indexOf(child);
		TreeModelEvent ev = new TreeModelEvent(this, path, indices, newChildren);
		this.treeModel.fireTreeNodesInserted(ev);
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
		this.treeModel.log.info("isLeaf " + this + ": " + children);
		return children.size() == 0;
	}

}