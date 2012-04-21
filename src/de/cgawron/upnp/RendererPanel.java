package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import de.cgawron.upnp.tree.UPnPTreeCellRenderer;

public class RendererPanel extends JPanel
{
	JTree treeView;
	TreeModel model;

	RendererPanel(TreeModel model)
	{
		super();
		this.model = model;
		this.setPreferredSize(new Dimension(400, 400));
		setLayout(new BorderLayout(0, 0));

		treeView = new JTree();
		treeView.setAutoscrolls(true);
		treeView.setShowsRootHandles(true);
		treeView.setCellRenderer(new UPnPTreeCellRenderer());

		JScrollPane scrollPane = new JScrollPane(treeView);
		add(scrollPane);

		treeView.setModel(model);
	}

}
