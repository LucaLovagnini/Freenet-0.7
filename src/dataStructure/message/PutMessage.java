package dataStructure.message;

import control.KeysGenerator;
import dataStructure.DarkPeer;
import protocol.LinkableProtocol;
import protocol.MessageProtocol;

public class PutMessage extends ForwardMessage {

	public PutMessage(float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
	}
	
	/**
	 * Copy constructor used to clone an abstract class with final fields
	 * @param another
	 */
	public PutMessage(PutMessage another){
		super(another);
	}
	
	@Override
	public Object clone() {
		return new PutMessage(this);
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		//add this node to the list of visited nodes
		//debugging check: a node can't receive the same get message twice
		if(!allPeersVisited.add(sender))
			throw new RuntimeException("message "+this+" already seen "+sender.getID());
		if(sender.containsKey(this.messageLocationKey))
			throw new RuntimeException(sender.getID()+" already contains key "+this.messageLocationKey);
		// get the Linkable protocol of the sender FPeer to access to its neighbors
		// find the closest peer w.r.t. the content key which didn't receive this message already
		// this is useful when this put message is part of a replication process
		DarkPeer receiver = ((LinkableProtocol) sender.getProtocol(mp.getLpId()))
				.getClosestNeighbor(this.messageLocationKey, allPeersVisited);
		// special case: A generates PUT, forwards to B (which only neighbor is A), but they swap before B receives the message
		// conclusion: B want to forward PUT to A, but he can't since he already visited it
		// second special case: during replication, A receives PUT message, but all his neighbors already received it
		if(receiver == null){
			MessageProtocol.printPeerAction(sender, this, "NO NEIGHBORS AVAILABLE!");
			return;
		}
		//if sender is closer than the closest neigbor w.r.t the message key
		if(this.isCloserThan(sender.getLocationKey(), receiver.getLocationKey())){
			//then store the message location key
			sender.storeKey(this.messageLocationKey);
			//notify the key generator that the key has been stored somewhere (so we can do get operation on it)
			KeysGenerator.addStoredKey(this.messageLocationKey);
			MessageProtocol.printPeerAction(sender, this, "STORED HERE!");
		}
		//if this node is the closest one w.r.t. all the previous key, then HTL is reset (and is > 0 for sure)
		//otherwise, check if HTL>0 and in that case forward the message
		else if(this.isBestDistance(sender, sender.getDistanceFromLocationKey(this.messageLocationKey)) || this.getHTL()>0){
			mp.sendForwardMessage(sender, receiver, this);

		}
		else
			MessageProtocol.printPeerAction(sender, this, "DYING!");
	}


}
