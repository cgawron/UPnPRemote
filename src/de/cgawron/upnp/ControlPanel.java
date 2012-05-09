package de.cgawron.upnp;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.renderingcontrol.lastchange.ChannelVolume;

import de.cgawron.upnp.service.AVTransportProxy;
import de.cgawron.upnp.service.RenderingControlProxy;
import de.cgawron.upnp.tree.ContentTreeModel;
import de.cgawron.upnp.tree.DeviceTreeModel;
import de.cgawron.upnp.tree.DeviceTypeNode;
import de.cgawron.upnp.tree.RootNode;

public class ControlPanel extends JPanel implements ItemListener, PropertyChangeListener
{
	private static final long serialVersionUID = 1L;

	private static final String DRADIO_URL = "http://192.168.10.2/~cgawron/test.m3u";
	private static final String TEST_URL = "http://192.168.10.1:49100/mediapath/Lenovo-MemoryKey4GB-01/Ralph%20Towner/Chiaroscuro/04%20-%20Sacred%20Place.mp3";
	private static final String DRADIO_STREAM_URL = "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m";
	private final String url = DRADIO_URL;
	// private final String url = TEST_URL;
	private final DeviceTreeModel deviceModel;
	private final ContentTreeModel contentModel;
	private RemoteDevice renderer;
	private final UpnpService upnpService;
	private static Logger log = Logger.getLogger(ControlPanel.class.getName());

	private final JComboBox rendererSelectable;

	private AVTransportProxy avTransport;
	private RenderingControlProxy renderingControl;

	private final JSlider volume;
	private final JTextField avTransportURI;

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

	class MyListCellRenderer extends JLabel implements ListCellRenderer
	{

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			RemoteDevice device = (RemoteDevice) value;
			if (device == null)
				setText("<null>");
			else
				setText(device.getDisplayString() + " " + device.getDetails().getFriendlyName());
			return this;
		}

	}

	class ControlAction extends AbstractAction
	{

		ControlAction(String name)
		{
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (renderer == null)
				return;

			RemoteService service = renderer.findService(ServiceId.valueOf("urn:upnp-org:serviceId:AVTransport"));
			Action action = service.getAction(getValue(NAME).toString());
			if (action == null) {
				log.severe("Could not find action " + getValue(NAME));
			}
			ActionInvocation invocation = new ActionInvocation(action);
			ActionCallback callback = new ActionCallback(invocation) {

				@Override
				public void success(ActionInvocation invocation)
				{
					log.info("action succeeded: " + invocation.getAction());
				}

				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
				{
					log.severe("action failed: " + defaultMsg);
					throw new RuntimeException(defaultMsg, invocation.getFailure());
				}

			};
			invocation.setInput("InstanceID", new UnsignedIntegerFourBytes(0));
			if (getName() == "Play") {
				invocation.setInput("Speed", "1");
			}
			// getActionInvocation().setInput("Speed", speed);
			/*
			 * log.info("setting URI " + url + " on " +
			 * renderer.getDisplayString()); ActionCallback setURI = new
			 * SetAVTransportURI(renderer.findService(ServiceId.valueOf(
			 * "urn:upnp-org:serviceId:AVTransport")), url) {
			 * 
			 * @Override public void failure(ActionInvocation invocation,
			 * UpnpResponse operation, String defaultMsg) {
			 * log.severe("action failed: " + defaultMsg); throw new
			 * RuntimeException(invocation.getFailure()); }
			 * 
			 * @Override public void success(ActionInvocation invocation) {
			 * log.severe("setURI succeeded"); super.success(invocation); }
			 * 
			 * }; upnpService.getControlPoint().execute(setURI);
			 */

			upnpService.getControlPoint().execute(callback);
		}
	}

	public ControlPanel(final UpnpService upnpService, DeviceTreeModel deviceModel, ContentTreeModel contentModel)
	{
		super();
		this.upnpService = upnpService;
		this.deviceModel = deviceModel;
		this.contentModel = contentModel;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]
		{
				0, 0, 0, 0
		};
		gridBagLayout.rowHeights = new int[]
		{
				0, 0, 0, 0
		};
		gridBagLayout.columnWeights = new double[]
		{
				1.0, 1.0, 1.0, 1.0
		};
		gridBagLayout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0
		};
		setLayout(gridBagLayout);

		rendererSelectable = new JComboBox();
		rendererSelectable.addItemListener(this);
		rendererSelectable.setModel(new MyComboBoxModel(deviceModel));
		rendererSelectable.setRenderer(new MyListCellRenderer());
		GridBagConstraints gbc_rendererSelectable = new GridBagConstraints();
		gbc_rendererSelectable.gridwidth = 4;
		gbc_rendererSelectable.fill = GridBagConstraints.HORIZONTAL;
		gbc_rendererSelectable.insets = new Insets(0, 0, 5, 0);
		gbc_rendererSelectable.gridx = 0;
		gbc_rendererSelectable.gridy = 0;
		add(rendererSelectable, gbc_rendererSelectable);

		volume = new JSlider();
		volume.setMajorTickSpacing(10);
		volume.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				renderingControl.setVolume(volume.getValue());
			}
		});

		avTransportURI = new JTextField();
		avTransportURI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event)
			{
				log.info("action: " + avTransportURI.getText());
				avTransport.setAVTransportURI(avTransportURI.getText());
			}
		});
		GridBagConstraints gbc_avTransportURI = new GridBagConstraints();
		gbc_avTransportURI.gridwidth = 4;
		gbc_avTransportURI.insets = new Insets(0, 0, 5, 5);
		gbc_avTransportURI.fill = GridBagConstraints.HORIZONTAL;
		gbc_avTransportURI.gridx = 0;
		gbc_avTransportURI.gridy = 1;
		add(avTransportURI, gbc_avTransportURI);
		avTransportURI.setColumns(10);

		JButton btnPrevious = new JButton("Previous");
		btnPrevious.setAction(new ControlAction("Previous"));
		GridBagConstraints gbc_btnPrevious = new GridBagConstraints();
		gbc_btnPrevious.insets = new Insets(0, 0, 5, 5);
		gbc_btnPrevious.gridx = 0;
		gbc_btnPrevious.gridy = 2;
		add(btnPrevious, gbc_btnPrevious);

		JButton btnPlay = new JButton("Play");
		btnPlay.setAction(new ControlAction("Play"));
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(0, 0, 5, 5);
		gbc_btnPlay.gridx = 1;
		gbc_btnPlay.gridy = 2;
		add(btnPlay, gbc_btnPlay);

		JButton btnStop = new JButton("Stop");
		btnStop.setAction(new ControlAction("Stop"));
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.insets = new Insets(0, 0, 5, 5);
		gbc_btnStop.gridx = 2;
		gbc_btnStop.gridy = 2;
		add(btnStop, gbc_btnStop);

		JButton btnNext = new JButton("Next");
		btnNext.setAction(new ControlAction("Next"));
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.insets = new Insets(0, 0, 5, 0);
		gbc_btnNext.gridx = 3;
		gbc_btnNext.gridy = 2;
		add(btnNext, gbc_btnNext);
		GridBagConstraints gbc_volume = new GridBagConstraints();
		gbc_volume.fill = GridBagConstraints.HORIZONTAL;
		gbc_volume.gridwidth = 4;
		gbc_volume.gridx = 0;
		gbc_volume.gridy = 3;
		add(volume, gbc_volume);
	}

	@Override
	public void itemStateChanged(ItemEvent event)
	{
		if (event.getStateChange() == ItemEvent.SELECTED) {
			renderer = (RemoteDevice) event.getItem();
			avTransport = new AVTransportProxy(upnpService, renderer);
			renderingControl = new RenderingControlProxy(upnpService, renderer);
			avTransport.addPropertyChangeListener(this);
			renderingControl.addPropertyChangeListener(this);
		}
		else if (event.getStateChange() == ItemEvent.DESELECTED)
			renderer = null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		log.info("propertyChange: " + event.getPropertyName() + "=" + event.getNewValue());
		switch (event.getPropertyName()) {
		case "Volume":
			ChannelVolume vol = (ChannelVolume) event.getNewValue();
			volume.setValue(vol.getVolume());
			break;
		case "AVTransportURI":
			avTransportURI.setText(event.getNewValue().toString());
			break;
		default:
			break;

		}
	}
}
