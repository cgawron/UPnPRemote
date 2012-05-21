package de.cgawron.upnp.service;

import java.util.logging.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;

public class AVTransportProxy extends ServiceProxy
{
	RemoteService avTransport;
	private static Logger logger = Logger.getLogger(AVTransportProxy.class.getName());

	public AVTransportProxy(UpnpService upnpService, RemoteDevice mediaRenderer)
	{
		super(AVTransportVariable.ALL);
		this.upnpService = upnpService;
		this.avTransport = mediaRenderer.findService(ServiceId.valueOf("urn:upnp-org:serviceId:AVTransport"));
		this.callback = new MySubscriptionCallback(avTransport, 600);
		upnpService.getControlPoint().execute(callback);
	}

	public void setAVTransportURI(String uri)
	{
		logger.info("Setting AVTransportURI to " + uri);
		ActionCallback setAVTransportURI = new SetAVTransportURI(avTransport, uri) {

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
			{
				logger.severe(defaultMsg);
				throw new RuntimeException(invocation.getFailure());
			}
		};
		upnpService.getControlPoint().execute(setAVTransportURI);
	}
}
