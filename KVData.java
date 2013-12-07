
public class KVData {
	KVCommands command;
	StatusCode code;
	Key key;
	Object value;
	long id;
	

	public KVData(KVCommands command, Key key, Object value, long id, StatusCode code) {
        this.command = command;
        this.key = key;
        this.value = value;
        this.id = id;
        this.code = code;
	}

	public KVData(KVCommands command, long key, Object value, long id, StatusCode code) {
 		this(command, new Key(key, id), value, id, code);
	}

	public KVData(KVData data) {
		this(data.command, data.key, data.value, data.id, data.code);
	}
}
