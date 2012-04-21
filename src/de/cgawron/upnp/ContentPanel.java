package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import de.cgawron.upnp.tree.ContentTreeModel;

class ContentPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	JTree treeView;
	TreeModel model;

	ContentPanel(ContentTreeModel model)
	{
		super();
		this.model = model;
		this.setPreferredSize(new Dimension(400, 400));
		setLayout(new BorderLayout());
		treeView = new JTree();
		treeView.setShowsRootHandles(true);
		JScrollPane scrollPane = new JScrollPane(treeView);
		add(scrollPane);

		treeView.setModel(model);
	}
}