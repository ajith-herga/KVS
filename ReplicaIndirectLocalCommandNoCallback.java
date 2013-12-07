
public class ReplicaIndirectLocalCommandNoCallback extends LocalCommand {

	ReplicaIndirectLocalCommandNoCallback(KVData data, KVStore kvStore) {
		super(data, kvStore);
	}

	@Override
	public void callback(KVData cR) {
          //Do nothing.
	}

}
