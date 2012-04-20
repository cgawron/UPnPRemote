package de.cgawron.upnp.tree;

import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.teleal.cling.model.meta.RemoteService;

public class AbstractUPnPTreeModel
{
	Logger log = Logger.getLogger(DeviceTreeModel.class.getName());

	private final Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

	public void initializeChildren(AbstractNode<?, ? extends Node<?>> node)
	{
		if (node.object.getClass() == Void.class) {
			// Do nothing
		}
		else if (node instanceof DeviceNode) {
			DeviceNode p = (DeviceNode) node;
			RemoteService[] services = p.object.getServices();
			for (RemoteService service : services) {
				p.children.add(new ServiceNode(p, service));
			}
		}
	}

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
