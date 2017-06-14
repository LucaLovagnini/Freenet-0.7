package dataStructure.message;

import dataStructure.DarkPeer;
import dataStructure.message.Message;
import protocol.MessageProtocol;

public abstract class Message implements Cloneable {
	private static long nextMessageId = 0;
	public final long  messageId;
	public final float messageLocationKey;
	//message's hops-to-live counter. We set it as private so we are sure that we can only decrease and reset it, nothing else
	private int HTL;
	//closest location w.r.t. messageLocationKey met up to now
	private float bestLocationKey;
	//peer who sent the message
	private DarkPeer previousDarkPeer = null;

	public Message(float messageLocationKey, int HTL){
		this.messageId = nextMessageId++;
		this.messageLocationKey = messageLocationKey;
		this.HTL = HTL;
		this.bestLocationKey = -1;
	}
	
	/**
	 * Copy constructor necessary to clone objects with final fields (implemented in each concrete class)
	 * @param another the other (concrete) {@code Message} used during {@code clone()}
	 */
	protected Message(Message another)
	{
		this.bestLocationKey = another.bestLocationKey;
		this.HTL = another.HTL;
		this.messageId = another.messageId;
		this.messageLocationKey = another.messageLocationKey;
		this.previousDarkPeer = another.previousDarkPeer;
	}
	
	@Override
	public String toString(){
		return "mId="+messageId+" locKey="+messageLocationKey+" HTL="+HTL+" bestLocKey="+bestLocationKey;
	}
	
	public int decreaseHTL() {
		return HTL--;
	}
	
	public int getHTL(){
		return HTL;
	}
	
	public DarkPeer getPreviousDarkPeer() {
		return previousDarkPeer;
	}

	public void setPreviousDarkPeer(DarkPeer previousDarkPeer) {
		this.previousDarkPeer = previousDarkPeer;
	}
	

	/**
	 * Check if the message has just been generated or not
	 * @return true if the messasge has been seent at least once (there is a {@code previousDarkPeer}, false otherwise
	 */
	public boolean hasBeenSent(){
		return this.previousDarkPeer != null;
	}
	
	public boolean isCloserThan(double locationKey1, double locationKey2){
		
	}
	
	public abstract void doMessageAction(DarkPeer sender, MessageProtocol mp);
	
	public abstract Object clone(); 
}
