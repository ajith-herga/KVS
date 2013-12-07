
public class KVDataReplicaBulkReq extends KVDataBulkWithSrcHost {
	long milisPrimary = 0;
	
	public KVDataReplicaBulkReq(KVData[] data, TableEntry srcHostEntry) {
		super(data,srcHostEntry);
		milisPrimary = System.currentTimeMillis();
	}

}
