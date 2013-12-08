import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

public class DirectLocalCommand extends PrimaryReplicaCommand {
	KVClientRequestServer.Worker sourceWorker;
	
	DirectLocalCommand(KVClientRequestServer.Worker sW, KVData data, KVStore kvStore,
			GossipTransmitter txObj,
			ConcurrentHashMap<String,TableEntry> membTable,
			TableEntry selfEntry, List<ICommand> replicaCommands) {
		super(data, kvStore, txObj, membTable, selfEntry, replicaCommands);
		this.sourceWorker = sW;
	}

	public void callback(KVData cR) {
		Gson gson = new Gson();
		MarshalledClientData mcD = new MarshalledClientData(cR); 
		String tx = gson.toJson(mcD);
		//System.out.println("DLcommand Callback: " + tx);
		sourceWorker.send(tx);
		sourceWorker.closeClient();
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return data.id;
	}
	
}
