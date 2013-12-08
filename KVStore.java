import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

// allow for migrating to another host
// allow for addition to this else migration to another host.
// allow for bulk transfer to another host, if table is given
public class KVStore {
	ConcurrentHashMap<Key, Object> store = null;
	BufferedWriter bw = null;
	ConcurrentHashMap<String,TableEntry> membTable = null;
	
	KVStore(BufferedWriter bw, ConcurrentHashMap<String,TableEntry> membTable) {
		store = new ConcurrentHashMap<Key, Object>();
		this.bw = bw;
		this.membTable = membTable;
	}

	public  Object addKeyValue(Key key, Object value) {

		return store.put(key, value);
	}
	
	public Object removeKey(Key key) {
		return store.remove(key);
	}
	
	public Object updateKeyValue(Key key, Object value) {
		if (store.containsKey(key)) {
			return addKeyValue(key, value);
		} else {
			return null;
		}
	}

	public Object lookupKey(Key key) {
		return store.get(key);
	}
	
	public KVData[] getKVDataForMachine(TableEntry destHostEntry, KVCommands command) {
		ArrayList<KVData> kvdata = new ArrayList<KVData>(store.size());
		TableEntry prevHost = HashUtility.findPreviousMachine(membTable, destHostEntry.hashString);
		System.out.println("Previous for " + destHostEntry.id + " is " + prevHost.id + " KVStore size " + store.size());
		System.out.println("Previous Hash:" + prevHost.hashString);
		System.out.println("Current Hash:" + destHostEntry.hashString);
		if (prevHost.compareTo(destHostEntry) == 0) {
			return null;
		}
		for (Key key: store.keySet()) {
			boolean rangeCondition1 = prevHost.compareTo(destHostEntry) < 0 &&
					key.keyHash.compareTo(prevHost.hashString) >= 0 && 
					key.keyHash.compareTo(destHostEntry.hashString) < 0;
			boolean rangeCondition2 = prevHost.compareTo(destHostEntry) > 0 &&
					(key.keyHash.compareTo(prevHost.hashString) >= 0 || 
					key.keyHash.compareTo(destHostEntry.hashString) < 0);
			if (rangeCondition1 || rangeCondition2) {
				Object value = store.get(key);
				KVData temp = new KVData(command, key, value, key.timestamp, StatusCode.SUCCESS, 1);
				System.out.println("Found KVData " + temp);
				kvdata.add(temp);
			}
			System.out.println("Keyhash " + key.keyHash + " Object " + store.get(key));
		}
		if (kvdata.isEmpty()) {
			return null;
		}
		int i = 0;
		KVData[] retKv = new KVData[kvdata.size()];
		for (KVData data : kvdata) {
			retKv[i++] = data;
		}
		return retKv;
	}
}
