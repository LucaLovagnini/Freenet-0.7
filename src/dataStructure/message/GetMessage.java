package dataStructure.message;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public class GetMessage extends ForwardMessage {

	public GetMessage(float messageLocationKey, int HTL, long originId) {
		super(messageLocationKey, HTL, originId);
	}
	
	public GetMessage(GetMessage another){
		super(another);
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		//if the node has the searched content
		if(sender.containsKey(this.messageLocationKey)){
			MessageProtocol.printPeerAction(sender, " has key "+this.messageLocationKey+", routing back the answer");
		}
	}
	
	@Override
	public Object clone() {
		return new GetMessage(this);
	}


}
