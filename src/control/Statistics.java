package control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import peersim.config.Configuration;
import peersim.core.Network;
import protocol.LinkableProtocol;
import dataStructure.DarkPeer;


public class Statistics implements peersim.core.Control
{
	// linkable protocol id
	private final int lpId;
	// dataset name that we are going to use in the experiment
	private final String network;
	// if false, don't do statistics
	private final boolean doStatistics;

	public Statistics(String prefix) 
	{
		this.lpId = Configuration.getPid(prefix + ".lpId");
		// get the dataset file absolute path
		String networkPath = Configuration.getString(prefix + ".networkPath");
		// get only the filename (without extension)
		this.network = networkPath.substring(networkPath.lastIndexOf('/') + 1, networkPath.lastIndexOf('.'));
		this.doStatistics = Configuration.getBoolean(prefix+".doStatistics");
	} 

	private void computeClusterCoefficient(PrintWriter statisticsFile) 
	{	

		statisticsFile.println("DarkId"+ "\t"+"ClusterCoefficient");

		// cluster coefficient is computed on nodes who has at least two neighbors
		// this variable counts the number of nodes which satisfy this condition
		int validDarkPeers = 0;
		final int networkSize = Network.size();

		// for each DarkPeer of the overlay network
		for (int i = 0; i < networkSize; i++)
		{
			// get its linkable protocol and its degree
			DarkPeer darkPeer = (DarkPeer) Network.get(i);
			LinkableProtocol v_lp = (LinkableProtocol) darkPeer.getProtocol(lpId);
			float darkPeerDegree = v_lp.degree();

			// if node doesn't have at least 2 neighbors
			if (darkPeerDegree < 2)
				continue;

			// variable that represent the value |Eind(v)|
			float neighborsEdges = 0;
			
			//for each neighbor
			for(DarkPeer firstDarkNeighbor : v_lp.getNeighborTree())
				//for each firstDarkNeighbor's neighbor
				for (DarkPeer secondDarkNeighbor :  ((LinkableProtocol) firstDarkNeighbor.getProtocol(lpId)).getNeighborTree())
					// if secondDarkNeighbor is not darkPeer and it exists an edge between first and second dark neighbors
					if (secondDarkNeighbor != darkPeer && v_lp.contains(secondDarkNeighbor))
						neighborsEdges++;

			// compute darkPeer's cluster coefficient
			float clusterCoefficient = neighborsEdges / (darkPeerDegree * (darkPeerDegree - 1));
			// write it to file
			statisticsFile.println((validDarkPeers + 1) + "\t" + clusterCoefficient);

			// increase nodes counter
			validDarkPeers++;
		}

		// write the number of peers used to compute the cluster coefficient
		statisticsFile.println("validDarkPeers"+"\t" + validDarkPeers);

	}

	private void computeDegreePerNode(PrintWriter statisticsFile) 
	{
			statisticsFile.println("DarkId"+"\t"+"Degree");
			// for each DarkPeer
			for (int i = 0; i < Network.size(); i++)
			{
				DarkPeer DarkPeer = (DarkPeer) Network.get(i);
				//get the linkable protocol
				LinkableProtocol DarkPeer_lp = (LinkableProtocol) DarkPeer.getProtocol(lpId);
				// write its id and degree to file
				statisticsFile.println(DarkPeer.darkId + "\t" + DarkPeer_lp.degree());
			}
	}

	private int[][] shortestPath(int networkSize)
	{
		// distMatrix[i][j] = distance between i-th and j-th dark peer
		int [][] distMatrix = new int [networkSize][networkSize];

		// distance between a node and itself = 0
		// otherwise, initialize it with biggest integer value
		for (int k = 0; k < networkSize; k++)
			for (int i = 0; i < networkSize; i++)
				distMatrix[k][i] = ((k == i) ? 0 : Integer.MAX_VALUE);

		// distance between the i-th node and one of its neighbors = 1
		for (int i = 0; i < networkSize; i++)
		{
			for (DarkPeer darkPeer :  ((LinkableProtocol) ((DarkPeer) Network.get(i)).getProtocol(lpId)).getNeighborTree())
			{
				int index = darkPeer.getIndex();
				distMatrix[i][index] = 1;
			}
		}
		// compute shortest path between each pair of nodes
		for (int k = 0; k < networkSize; k++)
			for (int i = 0; i < networkSize; i++)
				for (int j = 0; j < networkSize; j++)
					if (distMatrix[i][j] > (distMatrix[i][k] + distMatrix[k][j]))
						distMatrix[i][j] = (distMatrix[i][k] + distMatrix[k][j]);
		return distMatrix;
	}
	
	private void computeDiameter(PrintWriter statisticsFile) 
	{
		final int networkSize = Network.size();

		//given the set of the shortest path between pair of nodes, this represent the longest path between them
		int longestShortestPath = -1;
				
		// compute shortest paths
		int [][] distMatrix = shortestPath(networkSize);
		

		// for each pair of DarkPeers
		for (int i = 0; i < networkSize; i++)
			for (int j = 0; j < networkSize; j++)		
				if (distMatrix[i][j] > longestShortestPath)
					longestShortestPath = distMatrix[i][j];
		
		//save memory by deleting the distMatrix
		distMatrix = null;
		System.gc();
	
		//write diameter to file
		statisticsFile.println("Diameter"+ "\t" + longestShortestPath);		
	}

	private void computeAverageShortestPathLength(PrintWriter statisticsFile)
	{
		final int networkSize = Network.size();
		double sumShPathsLength = 0.0;
		// computes all shortest paths between each pair of nodes of the network using Floyd-Warshall algorithm
		int [][] distMatrix = shortestPath(networkSize);
		// sum the shortest paths length between each pair of nodes of the network
		for (int i = 0; i < networkSize; i++)
			for (int j = 0; j < networkSize; j++)
				sumShPathsLength += distMatrix[i][j];
		//save memory by deleting the distMatrix
		distMatrix = null;
		System.gc();		
		// compute average shortest paths length
		double avgShPathLength = sumShPathsLength / new Double(Network.size() * Network.size());
		//write it to file
		statisticsFile.println("AvgShortesPathLength"+"\t " + avgShPathLength);
	}


	@Override
	public boolean execute() 
	{
		if(doStatistics){
			PrintWriter statisticsFile = null;
			try 
			{
				System.out.println("Doing network statistics...");
				statisticsFile = new PrintWriter(new BufferedWriter(new FileWriter("statistics" + this.network + ".csv", false)));
				final int networkSize = Network.size();
				statisticsFile.println("NetworkSize"+ "\t" + networkSize);
				computeDiameter(statisticsFile);
				computeAverageShortestPathLength(statisticsFile);
				computeDegreePerNode(statisticsFile); 
				computeClusterCoefficient(statisticsFile);
				System.out.println("Network statistics done!");
			}
			catch (IOException e)
			{
				System.out.println("Error during statistics file opening/writing: \n" + e.getMessage()); 
			}
			finally
			{
				// close the file
				if (statisticsFile != null)
					statisticsFile.close();
			}
		}
		return false;
	}

}
