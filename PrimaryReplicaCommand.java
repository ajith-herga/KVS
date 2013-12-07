import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class PrimaryReplicaCommand extends LocalCommand{
	int expectRepliesRx = 0;
	int quorumLevel = 0;
	ConcurrentHashMap<String,TableEntry> membTable = null;
	TableEntry selfEntry = null;
	GossipTransmitter txObj = null;
	List<ICommand> replicaCommands = null;
	
	public PrimaryReplicaCommand(KVData data, KVStore kvStore, GossipTransmitter txObj, ConcurrentHashMap<String,TableEntry> membTable,
			TableEntry selfEntry, List<ICommand> replicaCommands) {
		super(data, kvStore);
		
		this.selfEntry = selfEntry;
		this.txObj = txObj;
		this.membTable = membTable;
		this.replicaCommands = replicaCommands;
		expectRepliesRx = 2;
		quorumLevel = 3;
	}

	public void execute() {
		System.out.println("Execute LocalPrimary Replica" + data.key + "  " + data.value);
		KVData data1 = new KVData(data), data2 = new KVData(data);
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
		
		TableEntry[] sendReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
		
		if (sendReplicas == null) {
			//No replication
			callback(data);
			return;
		}
			
		RemoteReplicaCommand repl1 = new RemoteReplicaCommand(data1, sendReplicas[0], txObj, selfEntry, this);
		RemoteReplicaCommand repl2 = new RemoteReplicaCommand(data2, sendReplicas[1], txObj, selfEntry, this);
		
		synchronized(replicaCommands) {
			replicaCommands.add(repl1);
			replicaCommands.add(repl2);
		}
		System.out.println("Size at PrimaryReplica" + replicaCommands.size());
		repl1.execute();
		repl2.execute();
		if (quorumLevel == 1)
		callback(data);
	}
	
	public long getId() {
		// TODO Auto-generated method stub
		return data.id;
	}

}
