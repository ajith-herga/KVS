import java.util.List;


public class SwapKVwithinReplicas implements ICommand {
	KVData[] dataRemove = null, dataAdd = null;
	TableEntry[] newReplicas = null, oldReplicas = null;
	TableEntry[] SrcDest = null;
	boolean invalid = false;
	TableEntry selfEntry = null;
	ReplicaBulkAddOrDelRemote replicaSrc = null, replicaDest = null;
    GossipTransmitter txObj = null;
    List<ICommand> replicaCommands = null;
    
	public SwapKVwithinReplicas(TableEntry[] oldReplicas,
			TableEntry[] newReplicas, GossipTransmitter txObj, 
			KVStore kvStore, TableEntry selfEntry, List<ICommand> replicaCommands) {
		this.newReplicas = newReplicas;
		this.oldReplicas = oldReplicas;
		this.selfEntry = selfEntry;
		this.txObj = txObj;
		this.replicaCommands = replicaCommands;

		if (newReplicas == null || oldReplicas == null) {
			invalid = true;
		}
		SrcDest = new TableEntry[2];
		if (newReplicas[0] == oldReplicas[0]) {
			if (newReplicas[1] != oldReplicas[1]) {
				SrcDest[0] = oldReplicas[1];
				SrcDest[1] = newReplicas[1];
			}
		} else if (newReplicas[1] == oldReplicas[1]) {
			if (newReplicas[0] != oldReplicas[0]) {
				SrcDest[0] = oldReplicas[0];
				SrcDest[1] = newReplicas[0];
			}
		} else if (newReplicas[1] == oldReplicas[0]) {
			if (newReplicas[0] != oldReplicas[1]) {
				SrcDest[0] = oldReplicas[1];
				SrcDest[1] = newReplicas[0];
			}
		} else if (newReplicas[0] == oldReplicas[1]) {
			if (newReplicas[1] != oldReplicas[0]) {
				SrcDest[0] = oldReplicas[0];
				SrcDest[1] = newReplicas[1];
			}
		}
		
		if (SrcDest[0] == null) {
			System.out.println("No difference in locality");
			invalid = true;
		} else {
		    dataRemove = kvStore.getKVDataForMachine(selfEntry, KVCommands.DELETEKV);
		    if (dataRemove == null) {
		    	System.out.println("No keys with me");
		    	invalid = true;
		    }
		    dataAdd = kvStore.getKVDataForMachine(selfEntry, KVCommands.INSERTKV);
		}
		
		replicaSrc = new ReplicaBulkAddOrDelRemote(SrcDest[0], selfEntry, txObj, dataRemove, this);
		replicaDest = new ReplicaBulkAddOrDelRemote(SrcDest[1], selfEntry, txObj, dataAdd, null);
	}

	@Override
	public void execute() {
		if (invalid) { 
			return;
		}
		synchronized (replicaCommands) {
			replicaCommands.add(replicaSrc);
		}
		replicaSrc.execute();
	}

	@Override
	public void callback(KVData cR) {
		// TODO Auto-generated method stub
		if (invalid) { 
			return;
		}
		replicaDest.execute();
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
