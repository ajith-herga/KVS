
public class KVDataReplicaReq  extends KVDataWithSrcHost{
	long milisPrimary = 0;

	public KVDataReplicaReq(KVData data, TableEntry srcHostEntry) {
		super(data,srcHostEntry);
		milisPrimary = System.currentTimeMillis();
	}

}
