package protocol;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import dataStructure.DarkPeer;
import dataStructure.message.Message;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;

public class LinkableProtocol implements Linkable, Protocol {

	private TreeSet<DarkPeer> darkNeighbors;
	
	/**
	 * Class constructor. Initializes the class fields.
	 * @param prefix the prefix, in the configuration file, of the protocol. 
	 **/
	public LinkableProtocol(String prefix)
	{
		this.darkNeighbors = new TreeSet<DarkPeer>();
	}
	
	@Override
	public Object clone()
	{
		LinkableProtocol lp = null;
		try {
			lp = (LinkableProtocol) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			System.out.println("Error while cloning LinkableProtocol: "+e.getMessage());
			System.exit(1);
		}
		lp.darkNeighbors = new TreeSet<DarkPeer>();
		return lp;
	}
	
	public TreeSet<DarkPeer> getNeighborTree(){
		return darkNeighbors;
	}

	@Override
	public boolean addNeighbor(Node darkPeer) {
		return this.darkNeighbors.add((DarkPeer) darkPeer);
	}
	
	
	public boolean removeNeighbor(DarkPeer darkPeer)
	{
		return this.darkNeighbors.remove(darkPeer);
	}
	@Override
	public boolean contains(Node darkPeer) {
		return this.darkNeighbors.contains((DarkPeer) darkPeer);
	}

	@Override
	public int degree() {
		return this.darkNeighbors.size();
	}

	@Override
	public Node getNeighbor(int i) {
		if(i<0 || i > this.darkNeighbors.size())
			throw new IndexOutOfBoundsException();
		int c = 0;
		for(DarkPeer darkNeighbor : this.darkNeighbors){
			if(c++ == i)
				return darkNeighbor;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onKill() {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * This method returns true if {@code sender} is closer {@code message} key than any of his neighbors.
	 * Notice that the {@code TreeSet} structure helps to efficiently find the neighbors with smaller and bigger keys w.r.t. the message key.
	 * It is used by {@code PutMessage}.
	 * @param 
	 * @param 
	 * @return 
	 */
	public boolean isClosestThanNeighbors(DarkPeer sender, Message message){
		DarkPeer keyPeer = new DarkPeer(null, null, message.messageLocationKey);
		// get the set of all FPeers with location key strictly less than the passed location key
		NavigableSet<DarkPeer> smallerDarkPeers =  darkNeighbors.headSet(keyPeer, false);
		Iterator<DarkPeer> smallerIt = smallerDarkPeers.descendingIterator();
		//if there is a peer with a smaller key w.r.t. the message key...
		//and that peer is closer to the message key w.r.t. sender, then return false
		if(smallerIt.hasNext() && !message.isCloserThan(sender.getLocationKey(), smallerIt.next().getLocationKey()))
			return false;
		// get the set of all FPeers with location key strictly greater than the passed location key
		NavigableSet<DarkPeer> biggerDarkPeers =  darkNeighbors.tailSet(keyPeer, false);
		Iterator<DarkPeer> biggerIt = biggerDarkPeers.iterator();
		//if there is a peer with a bigger key w.r.t. the message key...
		//and that peer is closer to the message key w.r.t. sender, then return false
		if(biggerIt.hasNext() && !message.isCloserThan(sender.getLocationKey(), biggerIt.next().getLocationKey()))
			return false;
		//otherwise it means that sender is the closer w.r.t. the message than all his neighbors
		return true;
	}
		
	/**
	 * This message returns the closest neighbor w.r.t. {@code locationKey} which didn't receive this message yet.
	 * Notice how {@code TreeSet} helps to efficiently find the set of the closest neighbors w.r.t. {@code locationKey}.
	 * @param locationKey
	 * @param allPeersVisited
	 * @return A reference to the closest neighbor. If such a node doesn't exist, this method returns {@code null}.
	 */
	public DarkPeer getClosestNeighbor(float locationKey, HashSet<DarkPeer> allPeersVisited){
		DarkPeer keyPeer = new DarkPeer(null, null, locationKey);
		// get the set of all FPeers with location key strictly less than the passed location key
		NavigableSet<DarkPeer> smallerDarkPeers =  darkNeighbors.headSet(keyPeer, false);
		// get the set of all FPeers with location key strictly greater than the passed location key
		NavigableSet<DarkPeer> biggerDarkPeers =  darkNeighbors.tailSet(keyPeer, false);
		Iterator<DarkPeer> smallerIt = smallerDarkPeers.descendingIterator();
		Iterator<DarkPeer> biggerIt = biggerDarkPeers.iterator();
		DarkPeer chosenPeer =null;
		//we define a "suitable node" a node that didn't receive the message already
		do{
		//if there are no suitable neighbors with smaller key
		if(!smallerIt.hasNext())
			//if there are no suitable neighbors with bigger key
			if(!biggerIt.hasNext()){
				//we have sent the message to all the neighbors already
				chosenPeer = null;
				break;
			}
			//if there are suitable neighbors with bigger key
			else
				chosenPeer = biggerIt.next();
		else
			//if there are suitable neighbors with smaller key, but no suitable node with bigger key
			if(!biggerIt.hasNext())
				chosenPeer = smallerIt.next();
			//if there are suitable neighbors with both smaller and bigger keys
			else{
				DarkPeer biggerDarkPeer = biggerIt.next();
				DarkPeer smallerDarkPeer = smallerIt.next();
				//we can't check the chosen peer at the while condition
				//because otherwise we loose the not chosen peer (we called .next())
				//if the already visited peers include the bigger peer, then chose the smaller one
				if(allPeersVisited.contains(biggerDarkPeer))
					chosenPeer = smallerDarkPeer;
				//chose the bigger one if we didn't visited it, but we visited the smaller one
				else if(allPeersVisited.contains(smallerDarkPeer))
					chosenPeer = biggerDarkPeer;
				//if we didn't visit both of them, chose the one with smaller key
				else
					chosenPeer = biggerDarkPeer.getDistanceFromLocationKey(locationKey) > 
				smallerDarkPeer.getDistanceFromLocationKey(locationKey) ? smallerDarkPeer : biggerDarkPeer;
			}
		}
		//check if the chosen peer has already received this message
		while(allPeersVisited.contains(chosenPeer));	
		return chosenPeer;
	}

}
