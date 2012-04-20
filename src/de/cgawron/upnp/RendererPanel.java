package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
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
		setLayout(new BorderLayout());
		treeView = new JTree();
		treeView.setShowsRootHandles(true);
		treeView.setCellRenderer(new UPnPTreeCellRenderer());

		add(treeView, BorderLayout.CENTER);

		treeView.setModel(model);

		JLabel lblNewLabel = new JLabel("Render");
		add(lblNewLabel, BorderLayout.NORTH);
	}

}
