import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

// allow for migrating to another host
// allow for addition to this else migration to another host.
// allow for bulk transfer to another host, if table is given
public class KVStore {
	ConcurrentHashMap<Key, Object> store = null;
	BufferedWriter bw = null;

	KVStore(BufferedWriter bw) {
		store = new ConcurrentHashMap<Key, Object>();
		this.bw = bw;
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
	
	public KVData[] getAllKVData() {
		KVData[] kvdata = new KVData[store.size()];
		int i = 0;
		for (Key key: store.keySet()) {
				Object value = store.get(key);
				KVData temp = new KVData(KVCommands.INSERTKV, key, value, -1, StatusCode.SUCCESS);
				kvdata[i++] = temp;
		}

		if (i == 0) {
			return null;
		}
		return kvdata;
	}

	public KVData[] getKVDataForMachine(TableEntry destHostEntry) {
		ArrayList<KVData> kvdata = new ArrayList<KVData>(store.size());
		for (Key key: store.keySet()) {
			if (key.keyHash.compareTo(destHostEntry.hashString) <= 0) {
				Object value = store.get(key);
				KVData temp = new KVData(KVCommands.INSERTKV, key, value, -1, StatusCode.SUCCESS);
				kvdata.add(temp);
			}
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
