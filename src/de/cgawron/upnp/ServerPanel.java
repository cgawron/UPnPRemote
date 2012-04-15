package de.cgawron.upnp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JTree;

class ServerPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	JTree treeView;
	ServerTreeModel model;

	ServerPanel(ServerTreeModel model)
	{
		super();
		this.model = model;
		this.setMinimumSize(new Dimension(100, 100));
		GridBagLayout gridBagLayout = new GridBagLayout();
		/*
		 * gridBagLayout.columnWidths = new int[] { 183, 83, 0 };
		 * gridBagLayout.rowHeights = new int[] { 72, 0, 0 };
		 * gridBagLayout.columnWeights = new double[] { 0.0, 0.0,
		 * Double.MIN_VALUE }; gridBagLayout.rowWeights = new double[] { 0.0,
		 * 0.0, Double.MIN_VALUE };
		 */
		setLayout(gridBagLayout);
		treeView = new JTree();
		treeView.setShowsRootHandles(true);
		GridBagConstraints gbc_treeView = new GridBagConstraints();
		gbc_treeView.insets = new Insets(0, 0, 5, 0);
		gbc_treeView.weighty = 1.0;
		gbc_treeView.weightx = 1.0;
		gbc_treeView.fill = GridBagConstraints.BOTH;
		gbc_treeView.anchor = GridBagConstraints.NORTHWEST;
		gbc_treeView.gridx = 0;
		gbc_treeView.gridy = 0;
		add(treeView, gbc_treeView);

		treeView.setModel(model);
	}

}