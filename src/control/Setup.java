package control;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dataStructure.DarkPeer;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import protocol.LinkableProtocol;

public class Setup implements Control {
	
	private final String networkPath;
	private final String darkNode;
	private final int lpId;
	
	public Setup(String prefix){
		networkPath = Configuration.getString(prefix+".networkPath");
		darkNode	= Configuration.getString(prefix+".darkNode");
		lpId 		= Configuration.getPid(prefix+".lpId");
		
	}

	public DarkPeer newPeer(String id){
		try
		{
			DarkPeer darkPeer = new DarkPeer(darkNode, id, KeysGenerator.getNextContentKeys());
			Network.add(darkPeer);
			return darkPeer;
		}
		catch (UnsupportedOperationException exc) 
		{
			System.out.println("ID=" + id + " FPeer's creation: " + exc.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean execute() {
		List<String> 	lines = new ArrayList<>();
		//Keep a reference between the node id and the correspondent DarkPeer object
		HashMap<String, DarkPeer> nodes = new HashMap<String, DarkPeer>();
		//Read network file
		try (Stream<String> stream = Files.lines(Paths.get(networkPath))) {
	        lines = stream.collect(Collectors.toList());
			for(String line : lines){
				String[] peerPair = line.split(",");
				System.out.println("values[0]="+peerPair[0]+" values[1]="+peerPair[1]);
				DarkPeer first 	= nodes.get(peerPair[0]);
				DarkPeer second	= nodes.get(peerPair[1]);
				//if null -> never seen this peer before
				if(first == null){
					first 	= newPeer(peerPair[0]);
					nodes.put(peerPair[0], first);
				}
				if(second == null){
					second 	= newPeer(peerPair[1]);
					nodes.put(peerPair[1], second);
				}
				//	create double link
				// 	creating (first, second)
				((LinkableProtocol) first.getProtocol(lpId)).addNeighbor(second);
				// 	creating (second, first)
				((LinkableProtocol) second.getProtocol(lpId)).addNeighbor(first);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		System.out.println(Network.size());
		return false;
	}

}
