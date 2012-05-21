package de.cgawron.upnp.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.lastchange.Event;
import org.teleal.cling.support.lastchange.EventedValue;
import org.teleal.cling.support.lastchange.LastChangeParser;

public class ServiceProxy
{
	protected UpnpService upnpService;
	protected SubscriptionCallback callback;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	// protected LastChange lastChange;
	protected UnsignedIntegerFourBytes id = new UnsignedIntegerFourBytes(0);
	protected LastChangeParser parser;
	protected Set<Class<? extends EventedValue>> expectedValues;
	protected Map<Class<? extends EventedValue>, EventedValue> values = new HashMap();

	private static Logger logger = Logger.getLogger(ServiceProxy.class.getName());

	class MySubscriptionCallback extends SubscriptionCallback
	{

		protected MySubscriptionCallback(Service service, int requestedDurationSeconds)
		{
			super(service, requestedDurationSeconds);
			logger.info("Subscribing to " + service);
		}

		@Override
		protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg)
		{
			logger.log(Level.SEVERE, defaultMsg, exception);
		}

		@Override
		protected void established(GENASubscription subscription)
		{
			logger.info(subscription.toString());
		}

		@Override
		protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus)
		{
			logger.info(subscription.toString());
		}

		@Override
		protected void eventReceived(GENASubscription subscription)
		{
			logger.info(subscription.toString());
			Map<String, StateVariableValue> values = subscription.getCurrentValues();
			try {
				String lastChangeString = values.get("LastChange").toString();
				log.info("lastChange: " + lastChangeString);
				Event event = parser.parse(lastChangeString);
				for (Class<? extends EventedValue> vc : expectedValues) {
					EventedValue newValue = event.getEventedValue(id, vc);
					if (newValue != null) {
						EventedValue oldValue = ServiceProxy.this.values.get(vc);
						pcs.firePropertyChange(newValue.getName(), oldValue != null ? oldValue.getValue() : null, newValue.getValue());
						ServiceProxy.this.values.put(vc, newValue);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents)
		{
			logger.info(subscription.toString());
			// TODO Auto-generated method stub

		}

	}

	public ServiceProxy(Set<Class<? extends EventedValue>> expectedValues)
	{
		super();
		this.expectedValues = expectedValues;
		this.parser = new MyLastChangeParser(expectedValues);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		this.pcs.removePropertyChangeListener(listener);
	}

}