package dataStructure;

import java.util.HashSet;

import peersim.core.GeneralNode;

public class DarkPeer extends GeneralNode implements Comparable<DarkPeer> {

	private String 	darkId;
	private float	locationKey;
	private HashSet<Double> storedKeys = new HashSet<Double>();
	
	public DarkPeer(String prefix)
	{
		super(prefix);
	}
	
	public DarkPeer(String prefix, String darkId, float locationKey){
		super(prefix);
		this.darkId = darkId;
		this.locationKey = locationKey;
	}
	
	@Override
	public int compareTo(DarkPeer p) {
		final float plocationKey = p.getLocationKey();
		if(this.locationKey == plocationKey)
			return 0;
		else if(this.locationKey > plocationKey)
			return 1;
		else
			return -1;
	}

	public String getDarkId() {
		return darkId;
	}

	public float getLocationKey() {
		return locationKey;
	}
	
	public double getDistanceFromLocationKey(double locationKey)
	{
		final double dist = Math.abs(this.locationKey - locationKey);
		return Math.min(dist, 1 - dist);
	}
	
	public boolean containsKey(double locationKey)
	{
		return storedKeys.contains(locationKey);
	}


}
