package dataStructure.message;

import java.util.HashSet;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public abstract class ForwardMessage extends Message {

	//message's hops-to-live counter
	//we set it as private so we are sure that we can only decrease and reset it, nothing else
	//notice that only forward messages have a HTL
	private int HTL;
	//closest location w.r.t. messageLocationKey met up to now
	private double bestDistance = 1;

	/**
	 * Used when this forward message is created as response to a {@code BackwardMessage}.
	 * For example when node receives {@code GetNotFound} but can forward GetMessage to another neighbor.
	 * @param messageLocationKey
	 * @param HTL
	 * @param originalMessageId
	 */
	public ForwardMessage(float messageLocationKey, int HTL, double bestDistance, 
			long originalMessageId, int originalHTL, HashSet<DarkPeer> allPeersVisited) {
		super(messageLocationKey, originalMessageId, originalHTL, allPeersVisited);
		this.HTL = HTL;
		this.bestDistance = bestDistance;
	}
	
	/**
	 * Used when this is a new message
	 * @param messageLocationKey
	 * @param HTL
	 */
	public ForwardMessage(float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
		this.HTL = HTL;
		// TODO Auto-generated constructor stub
	}
	
	public ForwardMessage(ForwardMessage another){
		super(another);
	}
	
	public int decreaseHTL() {
		if(HTL==0)
			throw new RuntimeException("HTL can't be negative!");
		return HTL--;
	}
	
	public int getHTL(){
		return HTL;
	}
	

	public boolean isBestDistance(DarkPeer sender, double distance) {
		if(distance < bestDistance){
			MessageProtocol.printPeerAction(sender, this, "RESET HTL!");
			//update best distance
			bestDistance = distance;
			//reset HTL
			this.HTL = this.originalHTL;
			return true;
		}
		else
			return false;
	}
	
	public String toString(){
		return super.toString()+" HTL="+HTL;
	}
	
	public double getBestDistance(){
		return bestDistance;
	}

}
