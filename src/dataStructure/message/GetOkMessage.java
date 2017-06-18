package dataStructure.message;

import java.util.HashSet;
import java.util.Stack;

import dataStructure.DarkPeer;
import protocol.MessageProtocol;

public class GetOkMessage extends BackwardMessage {

	public GetOkMessage(float messageLocationKey, HashSet<DarkPeer> allPeersVisited,
			Stack<DarkPeer> routingPath, long originalMessageId, int originalHTL, int getMessageHTL, double getBestDistance, long hops){
		super(messageLocationKey, routingPath, originalMessageId, originalHTL, allPeersVisited, hops, true);
	}

	public GetOkMessage(GetOkMessage another) {
		super(another);
	}

	@Override
	public Object clone() {
		return new GetOkMessage(this);
	}
	
	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		//debug check: it's impossible that we receive a GetNotFoundMessage for a message that we have never seen before
		if(!allPeersVisited.contains(sender))
			throw new RuntimeException("Peer "+sender.darkId+" has never seen message "+this.originalMessageId);
		//simply route back the message
		mp.sendBackwardMessage(sender, this);
	}

}
