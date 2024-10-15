package co.kaustab.cdc.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class ConnectorOffsetTailInfo {

	private static int TAIL_SIZE = 100;
	
	private static Map<String, ArrayBlockingQueue<String>> tailInfo = new HashMap<>();
	

	public static Map<String, ArrayBlockingQueue<String>> getAll() {
		return tailInfo;
	}
	
	public static ArrayBlockingQueue<String> get(String connectorName) {
		return tailInfo.get(connectorName);
	}
	
	public static boolean add(String connectorName, String data) {
		ArrayBlockingQueue<String> newData = new ArrayBlockingQueue(TAIL_SIZE);
		if(tailInfo.get(connectorName) != null) {
			newData = tailInfo.get(connectorName);
			if(!newData.isEmpty() && newData.size() == TAIL_SIZE) {
				newData.poll();
			}
		}
		boolean v = newData.add(data);
		tailInfo.put(connectorName, newData);
		return v;
	}
}
