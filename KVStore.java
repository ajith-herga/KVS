import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

// allow for migrating to another host
// allow for addition to this else migration to another host.
// allow for bulk transfer to another host, if table is given
public class KVStore {
	ConcurrentHashMap<Key, Object> kvStore = null;
	BufferedWriter bw = null;

	KVStore(BufferedWriter bw) {
		kvStore = new ConcurrentHashMap<Key, Object>();
		this.bw = bw;
	}

	public  Object addKeyValue(Key key, Object value) {

		return kvStore.put(key, value);
	}
	
	public Object removeKey(Key key) {
		return kvStore.remove(key);
	}
	
	public Object updateKeyValue(Key key, Object value) {
		if (kvStore.contains(key)) {
			return addKeyValue(key, value);
		} else {
			return null;
		}
	}

	public Object lookupKey(Key key) {
		return kvStore.get(key);
	}
	
	public KVData[] getAllKVData() {
		ArrayList<KVData> kvdata = new ArrayList<KVData>(kvStore.size()); 
		for (Key key: kvStore.keySet()) {
				Object value = kvStore.get(key);
				KVData temp = new KVData(KVCommands.INSERTKV, key, value, -1, StatusCode.SUCCESS);
				kvdata.add(temp);
		}
		if (kvdata.isEmpty()) {
			return null;
		}
		return (KVData[])kvdata.toArray();
	}

	public KVData[] getKVDataForMachine(TableEntry destHostEntry) {
		ArrayList<KVData> kvdata = new ArrayList<KVData>(kvStore.size()); 
		for (Key key: kvStore.keySet()) {
			if (key.keyHash.compareTo(destHostEntry.hashString) <= 0) {
				Object value = kvStore.get(key);
				KVData temp = new KVData(KVCommands.INSERTKV, key, value, -1, StatusCode.SUCCESS);
				kvdata.add(temp);
			}
		}
		if (kvdata.isEmpty()) {
			return null;
		}
		return (KVData[])kvdata.toArray();
	}
}
