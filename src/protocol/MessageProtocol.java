package protocol;

import java.util.HashMap;
import java.util.Random;

import control.KeysGenerator;

import dataStructure.DarkPeer;
import dataStructure.MessageLog;
import dataStructure.message.GetMessage;
import dataStructure.message.Message;
import dataStructure.message.PutMessage;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.UniformRandomTransport;

public class MessageProtocol implements EDProtocol, CDProtocol {
	// protocol identifier of the Hybrid-Protocol itself
	private int mpId;
	// protocol identifier of the used Transport protocol
	private int urtId;
	// protocol identifier of the used Linkable protocol
	private int lpId;
	private int HTL;
	private int HTLSwap;
	private int topK;
	private int swapFreq;
	private int cacheInter;
	private int cacheFreq;
	private double getProb;
	//Hash map <id, log> for all messages sent, where:
	//id: message ID
	//log: message log, i.e. a pair <receivedFrom, sentTo> where:
	//receivedFrom: node who sent the received message 
	//sentTo: is the list of nodes who the message has been forwarded to 
	private HashMap<Long, MessageLog> messageLogs;
	
	public MessageProtocol(String prefix){
		this.mpId		= Configuration.getPid(prefix + ".mpId");
		this.urtId		= Configuration.getPid(prefix + ".urtId");
		this.lpId		= Configuration.getPid(prefix + ".lpId");
		this.HTL		= Configuration.getInt(prefix + ".HTL");
		this.HTLSwap	= Configuration.getInt(prefix + ".HTLSwap");
		this.topK		= Configuration.getInt(prefix + ".topK");
		this.swapFreq	= Configuration.getInt(prefix + ".swapFreq");
		this.cacheInter	= Configuration.getInt(prefix + ".cacheInter");
		this.cacheFreq	= Configuration.getInt(prefix + ".cacheFreq");
		this.getProb	= Configuration.getDouble(prefix + ".getProb");
	}
	
	private boolean doGet(){
		final float val = new Random(System.nanoTime()).nextFloat();
		return (val < getProb) ? true : false;

	}
	
	public void forwardMessage(DarkPeer sender, DarkPeer receiver, Message message){
		//add the message to log
		messageLogs.put(message.messageId, new MessageLog(message.getPreviousDarkPeer(), receiver));
		//decrease hops-to-live
		message.decreaseHTL();
		//set sender as the previous peer who managed the message
		message.setPreviousDarkPeer(sender);
		//get the transport protocol
		UniformRandomTransport urt = (UniformRandomTransport) sender.getProtocol(this.getUrtId());
		//send the message
		urt.send(sender, receiver, message, this.mpId);
	}
	
	/**
	 * @param peer  the overlay network's FPeer associated to the protocol that performs the cycle
	 * @param pid   the protocol identifier of the running protocol
	 **/
	@Override
	public void nextCycle(Node peer, int pid) {
		final DarkPeer darkPeer = (DarkPeer) peer;
		System.out.print("Time "+CDState.getTime()+" Peer id="+peer.getID()+" degree="+((LinkableProtocol)darkPeer.getProtocol(lpId)).degree());
		Message message;
		//TODO: add Swap case
		//generate get message
		if(doGet()){
			System.out.println(" doing a get message");
			message = new GetMessage(KeysGenerator.getNextContentKeys(), HTL);
		}
		//generate put message
		else{
			System.out.println(" doing a put message");
			message = new PutMessage(KeysGenerator.getNextContentKeys(), HTL);
		}
		message.doMessageAction(darkPeer, this);
	}

	@Override
	public void processEvent(Node peer, int pid, Object mex) {
		DarkPeer 	darkPeer = (DarkPeer) peer;
		Message		message = (Message) mex;
		System.out.println("Peer id="+peer.getID()+" received message: "+mex.toString());
		
		message.doMessageAction(darkPeer, this);
	}
	
	@Override
	public Object clone(){
		MessageProtocol mp = null;
		try
		{
			// invoke Object.clone()
			mp = (MessageProtocol) super.clone();
		} 
		catch (final CloneNotSupportedException e)
		{
			System.out.println("Error while cloning LinkableProtocol: "+e.getMessage());
			return null;
		}
		mp.cacheFreq = this.cacheFreq;
		mp.cacheInter = this.cacheInter;
		mp.getProb = this.getProb;
		mp.HTL = this.HTL;
		mp.HTLSwap = this.HTLSwap;
		mp.lpId = this.lpId;
		mp.mpId = this.mpId;
		mp.swapFreq = this.swapFreq;
		mp.topK = this.topK;
		return mp;
	}

	public int getLpId(){
		return lpId;
	}

	public int getUrtId() {
		return urtId;
	}
	
	public int getHTL() {
		return HTL;
	}

	
}
