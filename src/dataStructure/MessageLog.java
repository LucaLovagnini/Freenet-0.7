package dataStructure;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MessageLog {
	public final DarkPeer receivedFrom;
	public final Set<DarkPeer> sentTo;
	
	public MessageLog(DarkPeer receivedFrom, DarkPeer sentTo){
		this.receivedFrom = receivedFrom;
		this.sentTo = Collections.unmodifiableSet(new HashSet<DarkPeer>(Arrays.asList(sentTo)));
	}
	
	public MessageLog(DarkPeer sender, HashSet<DarkPeer> receivers){
		this.receivedFrom = sender;
		this.sentTo = Collections.unmodifiableSet(receivers);
	}
	
}
