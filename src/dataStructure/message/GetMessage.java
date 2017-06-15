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
		System.out.println("GET MESSAGE ACTION!");
	}
	
	@Override
	public Object clone() {
		return new GetMessage(this);
	}


}
