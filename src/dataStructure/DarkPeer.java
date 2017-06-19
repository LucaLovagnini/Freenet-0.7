package dataStructure;

import java.util.HashSet;

import control.KeysGenerator;
import dataStructure.message.Message;
import peersim.core.GeneralNode;
import protocol.MessageProtocol;

public class DarkPeer extends GeneralNode implements Comparable<DarkPeer> {

	public 	final String 	darkId;
	private float			locationKey;
	private HashSet<Float> storedKeys = new HashSet<Float>();

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
	
	public float getDistanceFromLocationKey(float locationKey)
	{
		final float dist = Math.abs(this.locationKey - locationKey);
		return Math.min(dist, 1 - dist);
	}
	
	public void storeKey(Message message, DarkPeer sender){
		float locationKey = message.messageLocationKey;
		if(!storedKeys.add(locationKey))
			throw new RuntimeException(sender.getID()+" already stores "+message.messageLocationKey);
		//notify the key generator that the key has been stored somewhere (so we can do get operation on it)
		KeysGenerator.addStoredKey(locationKey);
		MessageProtocol.printPeerAction(sender, message, "STORED HERE!");
	}
	
	public boolean containsKey(float locationKey)
	{
		return storedKeys.contains(locationKey);
	}
	
	public float getLocationKey() {
		return locationKey;
	}

	public void setLocationKey(float locationKey) {
		this.locationKey = locationKey;
	}
	
	public HashSet<Float> getStoredKeys() {
		return storedKeys;
	}

	public void setStoredKeys(HashSet<Float> storedKeys) {
		this.storedKeys = storedKeys;
	}


}
