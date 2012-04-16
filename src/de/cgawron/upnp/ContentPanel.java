package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTree;

class ContentPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	JTree treeView;
	ContentTreeModel model;

	ContentPanel(ContentTreeModel model)
	{
		super();
		this.model = model;
		this.setPreferredSize(new Dimension(400, 400));
		setLayout(new BorderLayout());
		treeView = new JTree();
		treeView.setShowsRootHandles(true);
		add(treeView, BorderLayout.CENTER);

		treeView.setModel(model);
	}

}