package de.cgawron.upnp.tree;

import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.MusicTrack;

public class TrackNode extends AbstractNode<MusicTrack, GenericNode>
{

	TrackNode(AbstractNode parent, MusicTrack track)
	{
		super(parent, track);
		for (PersonWithRole person : track.getArtists()) {
			children.add(new GenericNode(this, person.getRole() + " " + person.getName()));
		}
		for (Res res : track.getResources()) {
			children.add(new GenericNode(this, res.getValue()));
		}
		String storage = "null";
		if (track.getStorageMedium() != null)
			storage = track.getStorageMedium().toString();
		children.add(new GenericNode(this, storage));
	}

	@Override
	public String toString()
	{
		return "Track " + object.getLongDescription();
	}

}
