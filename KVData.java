
public class KVData {
	KVCommands command;
	StatusCode code;
	Key key;
	Object value;
	int level;
	long id;
	

	public KVData(KVCommands command, Key key, Object value, long id, StatusCode code, int level) {
        this.command = command;
        this.level = level;
        this.key = key;
        this.value = value;
        this.id = id;
        this.code = code;
	}

	public KVData(KVCommands command, String key, Object value, long id, StatusCode code, int level) {
 		this(command, new Key(key, id), value, id, code, level);
	}

	public KVData(KVData data) {
		this(data.command, data.key, data.value, data.id, data.code, data.level);
	}
	
	public String toString() {
		return code + " " + value;
	}
}
