import java.util.List;

public class MoveKeysToNewDest implements ICommand {
	int numberAcks = 2;
	KVData[] replicaData = null, destData = null;
	TableEntry[] sendoldReplicas = null;
	TableEntry dest = null, selfEntry = null;
	RemoteMoveAndDeleteBulkCommand  destRemote = null;
	ReplicaBulkAddOrDelRemote replica1 = null, replica2 = null;
	List<ICommand> replicaCommands = null;
	public MoveKeysToNewDest(TableEntry[] sendoldReplicas,
			TableEntry dest, TableEntry selfEntry, GossipTransmitter txObj,
			KVStore kvStore, List<ICommand> replicaCommands,
			KVData[] replicaData, KVData[] destData) {
		this.sendoldReplicas = sendoldReplicas;
		this.selfEntry = selfEntry;
		this.replicaData = replicaData;
		this.dest = dest;
		//To send my keys if necessary to the new guy
		if (replicaData == null) {
			return;
		}
		if (sendoldReplicas != null) {
			this.replica1 = new ReplicaBulkAddOrDelRemote(sendoldReplicas[0], selfEntry, txObj, replicaData, this);
			this.replica2 = new ReplicaBulkAddOrDelRemote(sendoldReplicas[1], selfEntry, txObj, replicaData, this);
		}
		this.replicaCommands = replicaCommands;
        // Setup dest replicaData
        this.destData = destData;
		this.destRemote = new RemoteMoveAndDeleteBulkCommand(dest, kvStore, txObj, destData);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		if (replicaData == null) {
			return;
		}
		if (sendoldReplicas == null) {
			destRemote.execute();
			return;
		}
		synchronized (replicaCommands) {
			replicaCommands.add(replica1);
			replicaCommands.add(replica2);
		}
		replica1.execute();
		replica2.execute();
	}

	@Override
	public void callback(KVData cR) {

		if (sendoldReplicas == null || numberAcks == 0 || replicaData == null) {
			return;
		}
		numberAcks--;
		if (numberAcks == 0) {
			destRemote.execute();
		}
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
