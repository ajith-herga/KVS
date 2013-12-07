import java.util.concurrent.ConcurrentHashMap;


public class MarshalledServerData {
	ConcurrentHashMap<String, TableEntry> membTable = null;
	KVDataWithSrcHost query = null;
	KVDataReplicaReq replicaQuery = null;
    KVData reply = null;
    KVDataReplicaReply replicaReply = null;
    KVData[] bulkQuery = null;
    KVDataReplicaBulkReq replicaBulkReq = null;
    KVDataReplicaBulkReply replicaBulkReply = null;

    public MarshalledServerData(KVDataWithSrcHost essentials) {
	    this.query = essentials;
	}

    public MarshalledServerData(KVDataReplicaReq essentials) {
	    this.replicaQuery = essentials;
	}
    
    public MarshalledServerData(KVDataReplicaReply essentials) {
	    this.replicaReply = essentials;
	}

    public MarshalledServerData(KVData cR) {
		this.reply = cR;
	}

	public MarshalledServerData(KVData[] cRs) {
		this.bulkQuery = cRs;
	}

	public MarshalledServerData(ConcurrentHashMap<String, TableEntry> membTable) {
		this.membTable = membTable;
	}

	public MarshalledServerData(KVDataReplicaBulkReq essentials) {
		this.replicaBulkReq = essentials;
	}

	public MarshalledServerData(KVDataReplicaBulkReply essentials) {
		this.replicaBulkReply = essentials;
	}
	
}
