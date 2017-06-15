package dataStructure;

import java.util.HashSet;

import peersim.core.GeneralNode;

public class DarkPeer extends GeneralNode implements Comparable<DarkPeer> {

	public 	final String 	darkId;
	private float			locationKey;
	private HashSet<Double> storedKeys = new HashSet<Double>();

	public DarkPeer(String prefix)
	{
		super(prefix);
		this.darkId = null;
		this.locationKey = 0;
	}
	
	public DarkPeer(String prefix, String darkId, float locationKey){
		super(prefix);
		this.darkId = darkId;
		this.locationKey = locationKey;
	}
	
	@Override
	public int compareTo(DarkPeer p) {
		if(this.locationKey == p.locationKey)
			return 0;
		else if(this.locationKey > p.locationKey)
			return 1;
		else
			return -1;
	}
	
	public double getDistanceFromLocationKey(double locationKey)
	{
		final double dist = Math.abs(this.locationKey - locationKey);
		return Math.min(dist, 1 - dist);
	}
	
	public void storeKey(double locationKey){
		storedKeys.add(locationKey);
	}
	
	public boolean containsKey(double locationKey)
	{
		return storedKeys.contains(locationKey);
	}
	
	public float getLocationKey() {
		return locationKey;
	}

	public void setLocationKey(float locationKey) {
		this.locationKey = locationKey;
	}
	
	public HashSet<Double> getStoredKeys() {
		return storedKeys;
	}

	public void setStoredKeys(HashSet<Double> storedKeys) {
		this.storedKeys = storedKeys;
	}


}
