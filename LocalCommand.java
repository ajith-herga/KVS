
public abstract class LocalCommand implements ICommand {
	KVData data;
	KVStore kvStore;
	LocalCommand(KVData data, KVStore kvStore) {
		this.data = data;
		this.kvStore = kvStore;
	}

	public void execute() {
		data.code = StatusCode.SUCCESS;
		switch(data.command) {
		case DELETEKV:
			data.value = kvStore.removeKey(data.key);
			break;
		case INSERTKV:
			data.value = kvStore.addKeyValue(data.key, data.value);
			break;
		case LOOKUPKV:
			data.value = kvStore.lookupKey(data.key);
			break;
		case MODIFYKV:
			data.value = kvStore.updateKeyValue(data.key, data.value);
			break;
		default:
			data.code = StatusCode.INVALID_COMMAND;
		}
		
		if (data.value == null && (data.code != StatusCode.INVALID_COMMAND) 
				&& data.command != KVCommands.INSERTKV) {
			data.code = StatusCode.FAILURE;
		}			
		callback(data);
	}
	
	public long getId() {
		// TODO Auto-generated method stub
		return data.id;
	}

}
