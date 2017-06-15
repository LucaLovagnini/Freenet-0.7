package protocol;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.AbstractCollection;
import java.util.Iterator;

import dataStructure.DarkPeer;
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
	
	public DarkPeer getClosestNeighbor(float locationKey){
		DarkPeer keyPeer = new DarkPeer(null, null, locationKey);
		// get the set of all FPeers with location key strictly less than the passed location key
		NavigableSet<DarkPeer> smallerDarkPeers =  darkNeighbors.headSet(keyPeer, false);
		// get the set of all FPeers with location key strictly greater than the passed location key
		NavigableSet<DarkPeer> biggerDarkPeers =  darkNeighbors.tailSet(keyPeer, false);
		Iterator<DarkPeer> smallerIt = smallerDarkPeers.descendingIterator();
		Iterator<DarkPeer> biggerIt = biggerDarkPeers.iterator();
		if(!smallerIt.hasNext())
			if(!biggerIt.hasNext())
				throw new RuntimeException("Both smallerIt and biggerIt doesn't have elements!");
			else
				return biggerIt.next();
		else
			if(!biggerIt.hasNext())
				return smallerIt.next();
			else{
				DarkPeer biggerDarkPeer = biggerIt.next();
				DarkPeer smallerDarkPeer = smallerIt.next();
				return biggerDarkPeer.getDistanceFromLocationKey(locationKey) > 
				smallerDarkPeer.getDistanceFromLocationKey(locationKey) ? smallerDarkPeer : biggerDarkPeer;
			}
	}
	
	public <E extends AbstractCollection<T>, T> DarkPeer getClosestNeighbor(float locationKey, E alreadySeen){
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
				chosenPeer = biggerDarkPeer.getDistanceFromLocationKey(locationKey) > 
				smallerDarkPeer.getDistanceFromLocationKey(locationKey) ? smallerDarkPeer : biggerDarkPeer;
			}
		}
		//check if the chosen peer has already received this message
		while(alreadySeen.contains(chosenPeer));	
		return chosenPeer;
	}

}
