package dataStructure.message;

public abstract class BackwardMessage extends Message {
	
	public final long destinationid;

	public BackwardMessage(float messageLocationKey, long destinationId) {
		super(messageLocationKey);
		this.destinationid = destinationId;
		// TODO Auto-generated constructor stub
	}

	public BackwardMessage(BackwardMessage another){
		super(another);
		destinationid = another.destinationid;
	}

}
