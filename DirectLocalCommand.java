import com.google.gson.Gson;

public class DirectLocalCommand extends LocalCommand {
	KVClientRequestServer.Worker sourceWorker;
	
	DirectLocalCommand(KVClientRequestServer.Worker sW, KVData data, KVStore kvStore) {
		super(data, kvStore);
		this.sourceWorker = sW;
	}

	public void callback(KVData cR) {
		Gson gson = new Gson();
		String tx = gson.toJson(cR);
		sourceWorker.send(tx);
		sourceWorker.closeClient();
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return data.id;
	}
	
}
