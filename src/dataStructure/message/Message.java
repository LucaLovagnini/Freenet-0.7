package dataStructure.message;

import java.util.HashSet;

import dataStructure.DarkPeer;
import dataStructure.message.Message;
import protocol.MessageProtocol;

public abstract class Message implements Cloneable {
	private static long nextMessageId = 0;
	private long hops;
	//this field is required when this message refers to another message
	//e.g. : GetNotFoundMessage refers to a GetMessage with id originalMessageId
	//this is not always required (e.g. PutMessage)
	public final long  originalMessageId;
	public final float messageLocationKey;
	//peers who forwarded the message, used to avoid cycles
	//this is useful for all kind of messages, for example:
	//PutMessage: when we store the message in a node, we forward the message to all neighbors (and they must avoid cycles)
	//GetMessage: since GetMessage forwards the message to the closest neighbor (without considering his key), this can create cycles
	protected final HashSet<DarkPeer> allPeersVisited;
	//used as reference to reset HTL. Used both by:
	//ForwardMessage: in case the peer want to reset HTL
	//BackwardMessage: in case the BackwardMessage generates a ForwardMessage (e.g. GetNotFoundMessage generates GetMessage)
	public final int originalHTL;
	

	/**
	 * Constructor used when this message is generated as answer from another.
	 * For example, when we create {@code GetFoundMessage} we use the same id of
	 * the correspondent {@cod GetMessage}.
	 * @param messageLocationKey content id
	 * @param originalMessageId id of the original message generated
	 */
	public Message(float messageLocationKey, long originalMessageId, int originalHTL, HashSet<DarkPeer> allPeersVisited, long hops){
		this.originalMessageId = originalMessageId;
		this.messageLocationKey = messageLocationKey;
		this.originalHTL = originalHTL;
		this.allPeersVisited = allPeersVisited;
		this.hops = hops;
	}
	
	public Message(float messageLocationKey, int HTL){
		this.originalMessageId = nextMessageId++;
		this.messageLocationKey = messageLocationKey;
		this.originalHTL = HTL;
		this.allPeersVisited = new HashSet<DarkPeer>();
		this.hops = 0;
	}
	
	/**
	 * Copy constructor necessary to clone objects with final fields (implemented in each concrete class)
	 * @param another the other (concrete) {@code Message} used during {@code clone()}
	 */
	protected Message(Message another)
	{
		this.originalMessageId = another.originalMessageId;
		this.messageLocationKey = another.messageLocationKey;
		this.originalHTL = another.originalHTL;
		this.allPeersVisited = another.allPeersVisited;
		this.hops = another.hops;
	}
	
	@Override
	public String toString(){
		return "mId="+this.originalMessageId+" messageLocationKey="+messageLocationKey+" type="+this.getClass();
	}	
	
	public boolean isCloserThan(double locationKey1, double locationKey2){
		final double dist1 = Math.abs(locationKey1 - messageLocationKey);
		final double dist2 = Math.abs(locationKey2 - messageLocationKey);

		//the "=" is important, otherwise if they are at the same distance, a loop happens
		return (Math.min(dist1, 1 - dist1) <= Math.min(dist2, 1 - dist2));
	}
	
	public void addPeerVisited(DarkPeer receiver){
		//since allPeersVisited is shared and since all messages can be received by any peer only once
		if(!allPeersVisited.add(receiver))
			throw new RuntimeException("message "+this+" already seen "+receiver.getID());		
	}
	
	public long getHops(){
		return hops;
	}
	
	public void addHop() {
		hops++;
	} 
	
	public abstract void doMessageAction(DarkPeer sender, MessageProtocol mp);
	
	public abstract Object clone();
}
