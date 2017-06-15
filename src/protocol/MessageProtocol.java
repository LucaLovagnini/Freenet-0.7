package protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import control.KeysGenerator;

import dataStructure.DarkPeer;
import dataStructure.MessageLog;
import dataStructure.message.BackwardMessage;
import dataStructure.message.ForwardMessage;
import dataStructure.message.GetMessage;
import dataStructure.message.Message;
import dataStructure.message.PutMessage;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
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
	
	private boolean doGet(){
		final float val = new Random(System.nanoTime()).nextFloat();
		return (val < getProb) ? true : false;

	}
	
	public void sendForwardMessage(DarkPeer sender, DarkPeer receiver, ForwardMessage message){
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
	
	public void sendBackwardMessage(DarkPeer sender, BackwardMessage message){
		//check if sender is the destination node
		if(sender.getID() == message.destinationid)
			System.out.println("Peer id="+sender.getID()+" received answer meessage: "+message.toString());
	}
	
	/**
	 * @param peer  the overlay network's FPeer associated to the protocol that performs the cycle
	 * @param pid   the protocol identifier of the running protocol
	 **/
	@Override
	public void nextCycle(Node peer, int pid) {
		final DarkPeer darkPeer = (DarkPeer) peer;
		System.out.println("Time "+CDState.getTime()+" Peer id="+peer.getID());
		final LinkableProtocol lp = (LinkableProtocol) darkPeer.getProtocol(lpId);
		/*
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
			message = new PutMessage(KeysGenerator.getNextContentKeys(), HTL, darkPeer.getID());
		}
		message.doMessageAction(darkPeer, this);
		
		*/
		final long time = CDState.getTime();
		//check if we have to swap location key
		if(time %swapFreq == 0){
			//randomly select a neighbor
			final int peerToSwapIndex = (new Random(System.nanoTime())).nextInt(lp.degree());
			DarkPeer peerToSwap = (DarkPeer) lp.getNeighbor(peerToSwapIndex);
			System.out.println("Trying to swap with "+peerToSwap.darkId);
			tryToSwap(darkPeer, peerToSwap);
		}

	}
	
	private void addToNeighbors(DarkPeer toAdd, LinkableProtocol toAddLp){
		for(DarkPeer darkNeighbor : toAddLp.getNeighborTree()){
				if(!((LinkableProtocol) darkNeighbor.getProtocol(lpId)).addNeighbor(toAdd))
					throw new RuntimeException(darkNeighbor.getID()+" is already a neighbor of "+toAdd.getID());
		}
	}
	
	private void removeFromNeighbors(DarkPeer toRemove, DarkPeer toAvoid, LinkableProtocol toRemoveLp){
		for(DarkPeer darkNeighbor : toRemoveLp.getNeighborTree()){
			//if darkNeighbor is toAvoid, skip it
			if(darkNeighbor != toAvoid)
				if(!((LinkableProtocol) darkNeighbor.getProtocol(lpId)).removeNeighbor(toRemove))
					throw new RuntimeException(darkNeighbor.getID()+" is not a neighbor of "+toRemove.getID());
		}
	}

	private void tryToSwap(DarkPeer A, DarkPeer B) {
		//pa1: product of the distance between a and each of its neighbors (first term of D1 in the original paper)
		//pb1: product of the distance between b and each of its neighbors (second term of D1 in the original paper)
		//pa2: product of the distance between a and each of b's neighbors (second term of D2 in the original paper)
		//pb2: product of the distance between b and each of a's neighbors (first term of D2 in the original paper)
		float pa1=1, pa2=1, pb1=1, pb2=1;
		float ak = A.getLocationKey(), bk = B.getLocationKey();
		
		LinkableProtocol aLp = (LinkableProtocol) A.getProtocol(lpId);		
		//for each a's neighbor
		for(DarkPeer aNeighbor : aLp.getNeighborTree()){
			//Skip B, otherwise pb2 becomes 0
			if(aNeighbor == B)
				continue;
			//A's neighbor key
			float ank = aNeighbor.getLocationKey();
			pa1 *= Math.abs(ak - ank);
			pb2 *= Math.abs(bk - ank);
		}
		
		LinkableProtocol bLp = (LinkableProtocol) B.getProtocol(lpId);		
		//for each b's neighbor
		for(DarkPeer bNeighbor : bLp.getNeighborTree()){
			//Skip A, otherwise pa2 becomes 0
			if(bNeighbor == A)
				continue;
			//B's neighbor key
			float bnk = bNeighbor.getLocationKey();
			pa2 *= Math.abs(ak - bnk);
			pb1 *= Math.abs(bk - bnk);
		}
		
		double d1 = pa1*pb1, d2 = pa2*pb2;
		
		//swap if D2(A,B) < D1(A,B) OR with probability D1(A,B) / D2(A,B)
		if(d2 < d1 || (new Random(System.nanoTime())).nextFloat() < (d1/d2)){
			//we need to remove A (B) from each A's (B's) darkNeighbor in order not to break the TreeSet representation
			//we must not remove B (A) from A's neighbors, otherwise we cannot do the "vice versa"
			//remove A from each of its neighbors (except for B)
			removeFromNeighbors(A, B, aLp);
			//remove B from each of its neighbors (except for A)
			removeFromNeighbors(B, A, bLp);		
			
			//remove A from B and vice versa
			aLp.removeNeighbor(B);
			bLp.removeNeighbor(A);
			
			//swap keys
			A.setLocationKey(bk);
			B.setLocationKey(ak);
			
			//swap stored keys
			HashSet<Double> aStoredKeys = A.getStoredKeys();
			A.setStoredKeys(B.getStoredKeys());
			B.setStoredKeys(aStoredKeys);
			
			//here there is no node to avoid, since A (B) is not a neighbor of B (A) anymore
			//add A to each of its neighbors
			addToNeighbors(A, aLp);
			//add B to each of its neighbors	
			addToNeighbors(B, bLp);
			//add A to B and vice versa
			aLp.addNeighbor(B);
			bLp.addNeighbor(A);
		}
	}

	@Override
	public void processEvent(Node peer, int pid, Object mex) {
		DarkPeer 	darkPeer = (DarkPeer) peer;
		Message		message = (Message) mex;
		System.out.println("Peer id="+peer.getID()+" received message: "+mex.toString());
		
		message.doMessageAction(darkPeer, this);
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
