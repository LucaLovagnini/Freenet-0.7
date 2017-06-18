package protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

import control.KeysGenerator;

import dataStructure.DarkPeer;
import dataStructure.message.BackwardMessage;
import dataStructure.message.ForwardMessage;
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
	private double getProb;
	private static int HTL;
	private static int swapFreq;
	private static int replicationFactor;
	private static boolean verbose;
	private static boolean put = false;
		
	public MessageProtocol(String prefix){
		this.mpId							= Configuration.getPid(prefix + ".mpId");
		this.urtId							= Configuration.getPid(prefix + ".urtId");
		this.lpId							= Configuration.getPid(prefix + ".lpId");
		this.getProb						= Configuration.getDouble(prefix + ".getProb");
		MessageProtocol.replicationFactor	= Configuration.getInt(prefix + ".replicationFactor");
		MessageProtocol.HTL					= Configuration.getInt(prefix + ".HTL");
		MessageProtocol.verbose				= Configuration.getBoolean(prefix+".verbose");
		MessageProtocol.swapFreq						= Configuration.getInt(prefix + ".swapFreq");
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
			System.out.println("Error while cloning MessageProtocol: "+e.getMessage());
			return null;
		}
		mp.getProb = this.getProb;
		mp.lpId = this.lpId;
		mp.mpId = this.mpId;
		return mp;
	}
	
	public static void printPeerAction(DarkPeer peer, Message message){
		if(verbose)
			System.out.println("Time "+CDState.getTime()+" id="+peer.getID()+" locationKey="+peer.getLocationKey()+" "+message.toString());
	}
	
	public static void printPeerAction(DarkPeer peer, Message message, String bonus){
		if(verbose)
			System.out.println("Time "+CDState.getTime()+" id="+peer.getID()+" locationKey="+peer.getLocationKey()+" "+message.toString()+" "+bonus);		
	}
	
	public static void printPeerAction(DarkPeer peer, String bonus){
		if(verbose)
			System.out.println("Time "+CDState.getTime()+" id="+peer.getID()+" locationKey="+peer.getLocationKey()+" "+bonus);		
	}
	
	public static void writeStatistics(Message message, boolean result){
		PrintWriter writer = null;
		try{
			File statisticsFile = new File("statistics.csv");
			boolean exists = statisticsFile.exists();
		    writer = new PrintWriter(new FileOutputStream(statisticsFile, true));
		    if(!exists)
		    	writer.println("Time"+"\t"+"MessageType"+"\t"+"HTL"+"\t"+"ReplicationFactor"+"\t"+"SwapFrequency"+"\t"+"Result"+"\t"+"Hops");
		    writer.println(CDState.getTime()+"\t"+message.getClass()+"\t"+HTL+"\t"+replicationFactor+"\t"+swapFreq+"\t"+result+"\t"+message.getHops());
		} catch (IOException e) {
		   e.printStackTrace();
		}
		finally{
			if(writer != null)
				writer.close();
		}

	}
		
	private void sendMessage(DarkPeer sender, DarkPeer receiver, Message message){
		//get transport protocol
		UniformRandomTransport urt = (UniformRandomTransport) sender.getProtocol(this.getUrtId());
		//send the message
		urt.send(sender, receiver, message, this.mpId);
	}
	
	public void sendForwardMessage(DarkPeer sender, DarkPeer receiver, ForwardMessage message){
		printPeerAction(sender, message, "to "+receiver.getID());
		//add this node to the list of visited nodes
		//debugging check: a node can't receive the same get message twice
		message.addPeerVisited(receiver);
		//increase the total number of hops (used for statistics, not counting backward hops)
		message.addHop();
		//decrease hops-to-live
		message.decreaseHTL();
		sendMessage(sender, receiver, message);
	}
	
	public void sendBackwardMessage(DarkPeer sender, BackwardMessage message){
		//if the routing path is empty, then it means that this is the destination node
		if(message.getRoutingPathSize() == 0){
			printPeerAction(sender, message, "RESULT!");
			writeStatistics(message, message.successful);
		}
		else{
			//get the next node in the routing path
			DarkPeer previousPeer = message.popRoutingPath();
			sendMessage(sender, previousPeer, message);
		}
	}
	
	/**
	 * @param peer  the overlay network's FPeer associated to the protocol that performs the cycle
	 * @param pid   the protocol identifier of the running protocol
	 **/
	@Override
	public void nextCycle(Node peer, int pid) {
		
		//at the first cycle, first peer, do 2000 random put operations
		if(!put){
			System.out.println("Doing random puts...");
			for(int i=0; i<2000; i++){
				int randomNodeIndex = (new Random(System.nanoTime())).nextInt(Network.size());
				DarkPeer randomPeer = (DarkPeer) Network.get(randomNodeIndex);
				Message message = new PutMessage(randomPeer, KeysGenerator.getNextContentKey(), HTL, replicationFactor);
				message.doMessageAction(randomPeer, this);
			}
			System.out.println("Random puts done!");
			put = true;
		}
		//when we don't print anything, we just print time
		if(!verbose && peer.getID() == 0)
			System.out.println("time="+CDState.getTime());		
		
		final long time = CDState.getTime();
		//check if we have to swap location key
		if(peer.getID()== 0 && time %swapFreq == 0){
			for(int i=0; i<Network.size(); i++){
				DarkPeer swappingPeer = (DarkPeer) Network.get(i);
				//check if we have to swap location key
				if(time %swapFreq == 0){
					//randomly select a neighbor
					final int peerToSwapIndex = (new Random(System.nanoTime())).nextInt(((LinkableProtocol) swappingPeer.getProtocol(lpId)).degree());
					DarkPeer peerToSwap = (DarkPeer) ((LinkableProtocol) swappingPeer.getProtocol(lpId)).getNeighbor(peerToSwapIndex);
					tryToSwap(swappingPeer, peerToSwap);
				}
			}
		}

		
		if(peer.getID() == 0){
			for(int i=0; i<2000; i++){
				int randomNodeIndex = (new Random(System.nanoTime())).nextInt(Network.size());
				DarkPeer randomPeer = (DarkPeer) Network.get(randomNodeIndex);
				Message message = null;
				float getContentKey = KeysGenerator.getContentKeyForGet();
				if(getContentKey != -1){
					message = new GetMessage(randomPeer, KeysGenerator.getContentKeyForGet(), HTL);
					printPeerAction(randomPeer, message, "GET GENERATED!");
				}
				else
					printPeerAction(randomPeer, "doing a get message, but no key has been stored yet!");
		
				if(message != null)
					message.doMessageAction(randomPeer, this);
			}
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
			if(darkNeighbor != toAvoid){
				if(!((LinkableProtocol) darkNeighbor.getProtocol(lpId)).removeNeighbor(toRemove))
					throw new RuntimeException(darkNeighbor.getID()+" is not a neighbor of "+toRemove.getID());
			}
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
			HashSet<Float> aStoredKeys = A.getStoredKeys();
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
			printPeerAction(A, "swapped with "+B.getID());
		}
	}

	@Override
	public void processEvent(Node peer, int pid, Object mex) {
		DarkPeer 	darkPeer = (DarkPeer) peer;
		Message		message = (Message) mex;		
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
