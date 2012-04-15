package de.cgawron.upnp;

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class TreeModelSupport
{
	private final Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener listener)
	{
		if (listener != null && !listeners.contains(listener)) {
			listeners.addElement(listener);
		}
	}

	public void removeTreeModelListener(TreeModelListener listener)
	{
		if (listener != null) {
			listeners.removeElement(listener);
		}
	}

	public void fireTreeNodesChanged(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e)
	{
		for (TreeModelListener listener : listeners)
		{
			listener.treeStructureChanged(e);
		}
	}
}
