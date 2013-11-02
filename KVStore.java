import java.util.concurrent.ConcurrentHashMap;

// allow for migrating to another host
// allow for addition to this else migration to another host.
// allow for bulk transfer to another host, if table is given
public class KVStore {
	ConcurrentHashMap<Long, Object> kvStore = null;
	long hostHash;
	
	KVStore() {
		kvStore = new ConcurrentHashMap<Long, Object>();
	}

	public  Object addKeyValue(Long key, Object value) {
		return kvStore.put(key, value);
	}
	
	public Object removeKey(Long key) {
		return kvStore.remove(key);
	}
	
	public Object updateKeyValue(Long key, Object value) {
		return addKeyValue(key, value);
	}

	public Object lookupKey(Long key) {
		return kvStore.get(key);
	}
}
