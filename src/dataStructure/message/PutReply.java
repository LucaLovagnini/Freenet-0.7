package dataStructure.message;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public class PutReply extends BackwardMessage {
	
	public PutReply(float messageLocationKey, long destinationId){
		super(messageLocationKey, destinationId);
	}

	public PutReply(BackwardMessage another) {
		super(another);
	}
	
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return new PutReply(this);
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		// TODO Auto-generated method stub

	}


}
