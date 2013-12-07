
public class KVDataReplicaReply {
    KVData data;
    long milisPrimary = 0;
    
	public KVDataReplicaReply(KVDataReplicaReq rep) {
		this.data = rep.data;
		milisPrimary = rep.milisPrimary;
	}
}
