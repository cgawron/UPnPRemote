package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

import de.cgawron.upnp.tree.ContentTreeModel;
import de.cgawron.upnp.tree.DeviceTreeModel;
import de.cgawron.upnp.tree.DeviceTypeNode;
import de.cgawron.upnp.tree.RootNode;

public class ControlPanel extends JPanel implements ItemListener
{
	private static final long serialVersionUID = 1L;

	private static final String DRADIO_URL = "http://www.dradio.de/streaming/dlf.m3u";
	private static final String TEST_URL = "http://192.168.10.1:49100/mediapath/Lenovo-MemoryKey4GB-01/Ralph%20Towner/Chiaroscuro/04%20-%20Sacred%20Place.mp3";
	private static final String DRADIO_STREAM_URL = "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m";
	private final String url = DRADIO_URL;
	// private final String url = TEST_URL;
	private final DeviceTreeModel deviceModel;
	private final ContentTreeModel contentModel;
	private RemoteDevice renderer;
	private UpnpService upnpService;
	private static Logger log = Logger.getLogger(ControlPanel.class.getName());

	private final JComboBox rendererSelectable;

	@SuppressWarnings("serial")
	public class MyComboBoxModel extends AbstractListModel implements ComboBoxModel, TreeModelListener
	{
		DeviceTypeNode renderers;
		int selectedIndex;

		public MyComboBoxModel(DeviceTreeModel deviceModel)
		{
			deviceModel.addTreeModelListener(this);
			RootNode root = (RootNode) (deviceModel.getRoot());
			renderers = root.getChild(DeviceType.valueOf("urn:schemas-upnp-org:device:MediaRenderer:1"));
		}

		@Override
		public RemoteDevice getElementAt(int index)
		{
			if (index < getSize())
				return renderers.getChild(index).getObject();
			else
				return null;
		}

		@Override
		public int getSize()
		{
			return renderers.getChildCount();
		}

		@Override
		public RemoteDevice getSelectedItem()
		{
			return getElementAt(selectedIndex);
		}

		@Override
		public void setSelectedItem(Object selected)
		{
			for (int i = 0; i < getSize(); i++) {
				if (getElementAt(i).equals(selected))
					selectedIndex = i;
			}
		}

		@Override
		public void treeNodesChanged(TreeModelEvent arg0)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void treeNodesInserted(TreeModelEvent event)
		{
			log.info("tree node inserted " + event);
			if (event.getTreePath().getLastPathComponent().equals(renderers)) {
				for (int index : event.getChildIndices()) {
					log.info("child added: " + getElementAt(index));
					fireIntervalAdded(this, index, index);
				}
			}

		}

		@Override
		public void treeNodesRemoved(TreeModelEvent arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void treeStructureChanged(TreeModelEvent arg0)
		{
			// TODO Auto-generated method stub

		}

	}

	public ControlPanel(final UpnpService upnpService, DeviceTreeModel deviceModel, ContentTreeModel contentModel)
	{
		super();
		this.upnpService = upnpService;
		this.deviceModel = deviceModel;
		this.contentModel = contentModel;
		setLayout(new BorderLayout(0, 0));

		rendererSelectable = new JComboBox();
		rendererSelectable.addItemListener(this);
		rendererSelectable.setModel(new MyComboBoxModel(deviceModel));
		add(rendererSelectable);

		JButton btnPlay = new JButton();
		btnPlay.setAction(new AbstractAction("Play") {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (renderer == null)
					return;
				log.info("setting URI " + url + " on " + renderer.getDisplayString());
				ActionCallback setURI = new SetAVTransportURI(renderer.findService(ServiceId.valueOf("urn:upnp-org:serviceId:AVTransport")), url) {

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
					{
						log.severe("action failed: " + defaultMsg);
						throw new RuntimeException(invocation.getFailure());
					}

					@Override
					public void success(ActionInvocation invocation)
					{
						log.severe("setURI succeeded");
						super.success(invocation);
					}

				};
				upnpService.getControlPoint().execute(setURI);
				ActionCallback play = new Play(renderer.findService(ServiceId.valueOf("urn:upnp-org:serviceId:AVTransport"))) {

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
					{
						log.severe("action failed: " + defaultMsg);
						throw new RuntimeException(invocation.getFailure());
					}

					@Override
					public void success(ActionInvocation invocation)
					{
						log.severe("play succeeded");
						super.success(invocation);
					}

				};
				log.info("playing " + url + " on " + renderer.getDisplayString());
				upnpService.getControlPoint().execute(play);
			}

		});
		add(btnPlay, BorderLayout.SOUTH);
	}

	@Override
	public void itemStateChanged(ItemEvent event)
	{
		if (event.getStateChange() == ItemEvent.SELECTED)
			renderer = (RemoteDevice) event.getItem();
		else if (event.getStateChange() == ItemEvent.DESELECTED)
			renderer = null;
	}

}
