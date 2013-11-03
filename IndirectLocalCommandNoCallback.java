
public class IndirectLocalCommandNoCallback extends LocalCommand {

	IndirectLocalCommandNoCallback(KVData data, KVStore kvStore) {
		super(data, kvStore);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void callback(KVData cR) {
		//Do nothing
	}

}
