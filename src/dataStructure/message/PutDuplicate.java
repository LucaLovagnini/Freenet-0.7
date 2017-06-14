package dataStructure.message;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public class PutDuplicate extends Message {

	public PutDuplicate(float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
	}
	
	public PutDuplicate(PutDuplicate another){
		super(another);
	}
	
	@Override
	public Object clone() {
		return new PutDuplicate(this);
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		// TODO Auto-generated method stub
		System.out.println("PutDuplicate!!!");
	}


}
