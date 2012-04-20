package de.cgawron.upnp.tree;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class UPnPTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer
{
	private static final long serialVersionUID = 1L;

	private static final String KEY_DEVICE = "device.png";
	private static final String KEY_SERVICE = "service.png";
	private final Map<String, Icon> icons = new HashMap<String, Icon>();

	private static Icon getIcon(Object o)
	{
		return new ImageIcon();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf,
				int row, boolean hasFocus)
	{
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		label.setIcon(getIcon(value));
		return label;
	}
}
