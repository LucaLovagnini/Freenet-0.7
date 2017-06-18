package dataStructure.message;

import java.util.HashSet;
import java.util.Stack;

import dataStructure.DarkPeer;

public abstract class BackwardMessage extends Message {
	
	protected final Stack<DarkPeer> routingPath;
	public final boolean successful;
	
	public BackwardMessage(float messageLocationKey, Stack<DarkPeer> routingPath,
			long originalMessageId, int originalHTL, HashSet<DarkPeer> allPeersVisited, long hops, boolean successful) {
		super(messageLocationKey, originalMessageId, originalHTL, allPeersVisited, hops);
		this.routingPath = routingPath;
		this.successful = successful;
	}

	public BackwardMessage(BackwardMessage another){
		super(another);
		routingPath = another.routingPath;
		successful = another.successful;
	}

	public int getRoutingPathSize() {
		return routingPath.size();
	}
	
	public DarkPeer popRoutingPath() {
		return routingPath.pop();
	}

}
