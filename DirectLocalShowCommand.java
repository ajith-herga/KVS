import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;



public class DirectLocalShowCommand extends LocalCommand {

	KVClientRequestServer.Worker sourceWorker;
	

	public DirectLocalShowCommand(KVData data, KVClientRequestServer.Worker worker, KVStore kvStore) {
		super(data, kvStore);
		this.sourceWorker = worker;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		ConcurrentHashMap<Key, Object> store = kvStore.store;
		KVData[] allKVs = new KVData[store.size()];
		int i=0;
		for(Key key: store.keySet()){
			KVData data = new KVData(this.data.command, key, store.get(key), 0, StatusCode.SUCCESS);
			allKVs[i++] = data;
		}
		callback(allKVs);
	}

	@Override
	public void callback(KVData cR) {
		// do nothing

	}

	public void callback(KVData[] cR) {
		// do nothing
		Gson gson = new Gson();
		MarshalledClientData mcD = new MarshalledClientData(cR); 
		String tx = gson.toJson(mcD);
		//System.out.println("DLshowcommand: " + tx);
		sourceWorker.send(tx);
		sourceWorker.closeClient();
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
