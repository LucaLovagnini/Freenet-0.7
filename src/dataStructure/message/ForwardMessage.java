package dataStructure.message;


public abstract class ForwardMessage extends Message {

	//id of the node who created the message, it will be used as destination
	public final long originId;
	//message's hops-to-live counter
	//we set it as private so we are sure that we can only decrease and reset it, nothing else
	//notice that only forward messages have a HTL
	private int HTL;
	//closest location w.r.t. messageLocationKey met up to now
	private float bestLocationKey;

	public ForwardMessage(float messageLocationKey, int HTL, long originId) {
		super(messageLocationKey);
		this.HTL = HTL;
		this.originId = originId;
		// TODO Auto-generated constructor stub
	}
	
	public ForwardMessage(ForwardMessage another){
		super(another);
		originId = another.originId;
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
	
	@Override
	public String toString(){
		return super.toString()+" originId="+originId+" HTL="+HTL;
	}

}
