package control;

import java.util.ArrayList;
import java.util.Collections;

public class KeysGenerator {
	private final static ArrayList<Float> allContentKeys = GenerateContentKeys();
	private final static double maxKeys = 100000;
	private static int contentKeysIndex = 0;

	private static ArrayList<Float> GenerateContentKeys(){
		ArrayList<Float> allContentKeys = new ArrayList<Float>();
	    for (double i=1; i<maxKeys; i++) {
	        allContentKeys.add(new Float(i/maxKeys));
	    }
	    Collections.shuffle(allContentKeys);
	    return allContentKeys;
	}
	
	public static float getNextContentKeys(){
		if(contentKeysIndex >= allContentKeys.size())
			throw new ArrayIndexOutOfBoundsException
			("no keys available: contentKeysIndex="+contentKeysIndex+" allContentKeys.size()="+allContentKeys.size());
		float key = allContentKeys.get(contentKeysIndex++);
		return key;
	}

}
