package dataStructure.message;


public abstract class ForwardMessage extends Message {

	//message's hops-to-live counter
	//we set it as private so we are sure that we can only decrease and reset it, nothing else
	//notice that only forward messages have a HTL
	private int HTL;
	//closest location w.r.t. messageLocationKey met up to now
	private float bestLocationKey;

	public ForwardMessage(float messageLocationKey, int HTL, long originId, long originalMessageId) {
		super(messageLocationKey, originalMessageId);
		this.HTL = HTL;
		// TODO Auto-generated constructor stub
	}
	
	public ForwardMessage(float messageLocationKey, int HTL, long originId) {
		super(messageLocationKey);
		this.HTL = HTL;
		// TODO Auto-generated constructor stub
	}
	
	public ForwardMessage(ForwardMessage another){
		super(another);
	}
	
	public int decreaseHTL() {
		return HTL--;
	}
	
	public int getHTL(){
		return HTL;
	}
	
	public float getBestLocationKey() {
		return bestLocationKey;
	}

	public void setBestLocationKey(float bestLocationKey) {
		this.bestLocationKey = bestLocationKey;
	}
	
	public String toString(){
		return super.toString()+" HTL="+HTL;
	}

}
