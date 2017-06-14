package dataStructure.message;

import dataStructure.DarkPeer;
import peersim.cdsim.CDState;
import protocol.LinkableProtocol;
import protocol.MessageProtocol;

public class PutMessage extends Message {

	public PutMessage(float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
		// TODO Auto-generated constructor stub
	}
	
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
		if(sender.containsKey(this.messageLocationKey)){
			if(!this.hasBeenSent())
				throw new RuntimeException("Impossible case: sender="+sender.getID()+" contains key="+this.messageLocationKey);
			mp.forwardMessage(sender, this.getPreviousDarkPeer(), new PutDuplicate(this.messageLocationKey, mp.getHTL()));
			return;
		}
		
		// get the Linkable protocol of the sender FPeer to access to its neighbors
		final LinkableProtocol lp = (LinkableProtocol) sender.getProtocol(mp.getLpId());
		DarkPeer receiver = lp.getClosestNeighbor(this.messageLocationKey);
		
		System.out.print("Time "+CDState.getTime()+" Peer "+sender.getID()+" PUT message HTL="+this.getHTL());
		System.out.println(" forwarding to "+receiver.getID());
		mp.forwardMessage(sender, receiver, this);
	}


}
