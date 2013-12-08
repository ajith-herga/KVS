import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class IndirectLocalCommandNoCallback extends PrimaryReplicaCommand {

	IndirectLocalCommandNoCallback(KVData data, KVStore kvStore, GossipTransmitter txObj,
			ConcurrentHashMap<String, TableEntry> membTable, TableEntry selfEntry, List<ICommand> replicaCommands) {
		super(data, kvStore, txObj, membTable, selfEntry, replicaCommands);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void callback(KVData cR) {
		//Do nothing
	}

}
