package de.cgawron.upnp.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import org.teleal.cling.support.lastchange.EventedValue;
import org.teleal.cling.support.lastchange.LastChange;

public class ServiceProxy
{

	protected UpnpService upnpService;
	protected SubscriptionCallback callback;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected LastChange lastChange;

	private static Logger logger = Logger.getLogger(ServiceProxy.class.getName());

	class MySubscriptionCallback extends SubscriptionCallback
	{
		protected Set<Class<? extends EventedValue>> expectedValues;

		protected MySubscriptionCallback(Service service, int requestedDurationSeconds, Set<Class<? extends EventedValue>> expectedValues)
		{
			super(service, requestedDurationSeconds);
			this.expectedValues = expectedValues;
			logger.info("Subscribing to " + service.toString());
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
				lastChange = new LastChange(new MyLastChangeParser(expectedValues), lastChangeString);
				log.info(lastChange.toString());
				for (Class<? extends EventedValue> vc : expectedValues) {
					EventedValue v = lastChange.getEventedValue(0, vc);
					if (v == null) {
						log.info(vc.getName() + " not found");
					}
					else {
						log.info(v.getName() + "=" + v.getValue());
						pcs.firePropertyChange(v.getName(), null, v.getValue());
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

	public ServiceProxy()
	{
		super();
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