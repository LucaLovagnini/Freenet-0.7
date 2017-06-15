package dataStructure.message;

import java.util.Stack;

import dataStructure.DarkPeer;

public abstract class BackwardMessage extends Message {
	
	protected final Stack<DarkPeer> routingPath;
	
	public BackwardMessage(float messageLocationKey, Stack<DarkPeer> routingPath, long originalMessageId) {
		super(messageLocationKey, originalMessageId);
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
