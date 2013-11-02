
public class KVData {
	KVCommands command;
	StatusCode code;
	long key;
	Object value;
	long id;
	
	public KVData(KVCommands command, long key, Object value, TableEntry srcHostEntry, long id, StatusCode code) {
        this.command = command;
        this.key = key;
        this.value = value;
        this.id = id;
        this.code = code;
	}
}
