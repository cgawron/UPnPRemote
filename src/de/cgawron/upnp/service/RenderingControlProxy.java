package de.cgawron.upnp.service;

import java.util.logging.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

public class RenderingControlProxy extends ServiceProxy
{
	RemoteService renderingControl;
	private static Logger logger = Logger.getLogger(RenderingControlProxy.class.getName());

	public RenderingControlProxy(UpnpService upnpService, RemoteDevice mediaRenderer)
	{
		super(RenderingControlVariable.ALL);
		this.upnpService = upnpService;
		this.renderingControl = mediaRenderer.findService(ServiceId.valueOf("urn:upnp-org:serviceId:RenderingControl"));
		this.callback = new MySubscriptionCallback(renderingControl, 600);
		upnpService.getControlPoint().execute(callback);
	}

	public void setVolume(int value)
	{
		logger.info("Setting volume to " + value);
		ActionCallback setVolume = new SetVolume(renderingControl, value) {

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
			{
				logger.severe(defaultMsg);
				throw new RuntimeException(invocation.getFailure());
			}
		};
		upnpService.getControlPoint().execute(setVolume);
	}
}
