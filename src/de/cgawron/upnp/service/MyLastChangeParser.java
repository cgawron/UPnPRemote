package de.cgawron.upnp.service;

import java.util.Set;

import org.teleal.cling.support.lastchange.EventedValue;
import org.teleal.cling.support.lastchange.LastChangeParser;

public class MyLastChangeParser extends LastChangeParser
{
	Set<Class<? extends EventedValue>> expectedValues;

	public MyLastChangeParser(Set<Class<? extends EventedValue>> expectedValues)
	{
		this.expectedValues = expectedValues;
	}

	@Override
	protected String getNamespace()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Set<Class<? extends EventedValue>> getEventedVariables()
	{
		return expectedValues;
	}
}
