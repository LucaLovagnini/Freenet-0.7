package dataStructure.message;

import java.util.HashSet;

import dataStructure.DarkPeer;
import dataStructure.message.Message;
import protocol.MessageProtocol;

public abstract class Message implements Cloneable {
	private static long nextMessageId = 0;
	//this field is required when this message refers to another message
	//e.g. : GetNotFoundMessage refers to a GetMessage with id originalMessageId
	//this is not always required (e.g. PutMessage)
	public final long  originalMessageId;
	public final float messageLocationKey;
	//peer who sent the message
	private HashSet<DarkPeer> allPeersVisited;

	/**
	 * Constructor used when this message is generated as answer from another.
	 * For example, when we create {@code GetFoundMessage} we use the same id of
	 * the correspondent {@cod GetMessage}.
	 * @param messageLocationKey content id
	 * @param originalMessageId id of the original message generated
	 */
	public Message(float messageLocationKey, long originalMessageId){
		this.originalMessageId = originalMessageId;
		this.messageLocationKey = messageLocationKey;
	}
	
	public Message(float messageLocationKey){
		this.originalMessageId = nextMessageId++;
		this.messageLocationKey = messageLocationKey;
	}
	
	/**
	 * Copy constructor necessary to clone objects with final fields (implemented in each concrete class)
	 * @param another the other (concrete) {@code Message} used during {@code clone()}
	 */
	protected Message(Message another)
	{
		this.messageLocationKey = another.messageLocationKey;
		this.originalMessageId = another.originalMessageId;
		this.allPeersVisited = another.allPeersVisited;
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
	
	public abstract void doMessageAction(DarkPeer sender, MessageProtocol mp);
	
	public abstract Object clone(); 
}
