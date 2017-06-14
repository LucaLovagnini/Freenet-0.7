package dataStructure.message;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public class GetMessage extends Message {

	public GetMessage(float messageLocationKey, int HTL) {
		super(messageLocationKey, HTL);
	}
	
	public GetMessage(GetMessage another){
		super(another);
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		System.out.println("GET MESSAGE ACTION!");
	}
	
	@Override
	public Object clone() {
		return new GetMessage(this);
	}


}
