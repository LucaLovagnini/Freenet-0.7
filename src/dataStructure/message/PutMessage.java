package dataStructure.message;

import dataStructure.DarkPeer;
import protocol.LinkableProtocol;
import protocol.MessageProtocol;

public class PutMessage extends ForwardMessage {

	public PutMessage(DarkPeer sender, float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
		//visited peers are added in addPeerVisited
		//since this peer doesn't receive the message (but sends it), we add it now
		allPeersVisited.add(sender);
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
		// A node can't store a key of a message that he has never seen before
		if(sender.containsKey(this.messageLocationKey))
			throw new RuntimeException(sender.getID()+" already contains key "+this.messageLocationKey);
		//check if sender is closer w.r.t. the content key than ANY of his neighbors
		boolean isClosest = ((LinkableProtocol) sender.getProtocol(mp.getLpId())).isClosestThanNeighbors(sender, this);
		//store content in this node if sender is closer to the content key w.r.t. ANY of his neighbors
		if(isClosest)
			sender.storeKey(this, sender);		
		// get the Linkable protocol of the sender FPeer to access to its neighbors
		// find the closest peer to the content key which didn't receive this message already
		DarkPeer receiver = ((LinkableProtocol) sender.getProtocol(mp.getLpId()))
				.getClosestNeighbor(this.messageLocationKey, allPeersVisited);
		//if ALL the neighbors received this message already...
		if(receiver == null)
			MessageProtocol.printPeerAction(sender, this, "NO NEIGHBORS AVAILABLE!");
		//if there is at least one neighbor which didn't receive this message yet...
		else{
			//if this node is the closest one w.r.t. all the previous key, then HTL is reset
			this.isBestDistance(sender, sender.getDistanceFromLocationKey(this.messageLocationKey));
			//if there are still hops available...
			if(this.getHTL()>0){
				//if this node is closer to the key w.r.t. all his neighbors, then forward to all its neighbors
				if(isClosest)
					forwardToEverybody(sender, mp);
				//otherwise, if the closest node w.r.t. the message key which didn't receive the message yet is closer than this node
				//Then forward this message to it
				else if(!this.isCloserThan(sender.getLocationKey(), receiver.getLocationKey()))
					mp.sendForwardMessage(sender, receiver, this);
				else
					MessageProtocol.printPeerAction(sender, this, receiver.getID()+" isn't closer!");
			}
			else
				MessageProtocol.printPeerAction(sender, this, "DYING!");
		}
	}

	private void forwardToEverybody(DarkPeer sender, MessageProtocol mp) {
		//notice that we already reset HTL (if it was the case)
		LinkableProtocol lp = (LinkableProtocol) sender.getProtocol(mp.getLpId());
		//for each neighbor...
		for(DarkPeer darkNeighbor : lp.getNeighborTree()){
			//forward this Put only if the neighbor didn't receive the message already
			if(!this.allPeersVisited.contains(darkNeighbor)){
				PutMessage forwardedPut = (PutMessage) this.clone();
				mp.sendForwardMessage(sender, darkNeighbor, forwardedPut);
			}
		}
		
	}


}
