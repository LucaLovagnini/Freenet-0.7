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
		
		//The sender node contains already the key, which means 2 things:
		//	1. Since each Put key is unique, this should not be possible if {@code sender} created the messsage
		//  2. {@code sender} has already stored the key, send a {@code PutDuplicate} message
		if(sender.containsKey(this.messageLocationKey))
			return;

		// get the Linkable protocol of the sender FPeer to access to its neighbors
		DarkPeer receiver = ((LinkableProtocol) sender.getProtocol(mp.getLpId()))
				.getClosestNeighbor(this.messageLocationKey);
		//if sender is closer than the closest neigbor w.r.t the message key
		if(this.isCloserThan(sender.getLocationKey(), receiver.getLocationKey())){
			//then store the message location key
			sender.storeKey(this.messageLocationKey);
			//notify the key generator that the key has been stored somewhere (so we can do get operation on it)
			KeysGenerator.addStoredKey(this.messageLocationKey);
			MessageProtocol.printPeerAction(sender, this, "STORED HERE!");
		}
		else{
			//forward the message to the closest neighbor
			mp.sendForwardMessage(sender, receiver, this);

		}
		
	}


}
