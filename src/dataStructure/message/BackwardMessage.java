package dataStructure.message;

import java.util.HashSet;
import java.util.Stack;

import dataStructure.DarkPeer;

public abstract class BackwardMessage extends Message {
	
	protected final Stack<DarkPeer> routingPath;
	
	public BackwardMessage(float messageLocationKey, Stack<DarkPeer> routingPath,
			long originalMessageId, int originalHTL, HashSet<DarkPeer> allPeersVisited) {
		super(messageLocationKey, originalMessageId, originalHTL, allPeersVisited);
		this.routingPath = routingPath;
	}

	public BackwardMessage(BackwardMessage another){
		super(another);
		routingPath = another.routingPath;
	}

	public int getRoutingPathSize() {
		return routingPath.size();
	}
	
	public DarkPeer popRoutingPath() {
		return routingPath.pop();
	}

}
