package control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class KeysGenerator {
	private final static ArrayList<Float> allContentKeys = GenerateContentKeys();
	//set of keys successfully stored in nodes
	private final static ArrayList<Float> storedKeys = new ArrayList<Float>();
	private final static double maxKeys = 10000000;
	private static int contentKeysIndex = 0;

	private static ArrayList<Float> GenerateContentKeys(){
		ArrayList<Float> allContentKeys = new ArrayList<Float>();
		System.out.println("Generating keys...");
	    for (double i=1; i<maxKeys; i++) {
	        allContentKeys.add(new Float(i/maxKeys));
	    }
	    System.out.println("Shuffling keys...");
	    Collections.shuffle(allContentKeys);
	    System.out.println("Keys shuffled!");
	    return allContentKeys;
	}
	
	public static float getNextContentKey(){
		if(contentKeysIndex >= allContentKeys.size())
			throw new ArrayIndexOutOfBoundsException
			("no keys available: contentKeysIndex="+contentKeysIndex+" allContentKeys.size()="+allContentKeys.size());
		float key = allContentKeys.get(contentKeysIndex++);
		return key;
	}
	
	public static void addStoredKey(float key){
		storedKeys.add(key);
	}
	
	public static float getContentKeyForGet(){
		if(storedKeys.size() == 0)
			return -1;
		int randomStoredKeyIndex = (new Random(System.nanoTime())).nextInt(storedKeys.size());
		return storedKeys.get(randomStoredKeyIndex);
	}

}
