import java.util.concurrent.ConcurrentHashMap;


public class MarshalledServerData {
	ConcurrentHashMap<String, TableEntry> membTable = null;
	KVDataWithSrcHost query = null;
    KVData reply = null;
    KVData[] bulkQuery = null;

    public MarshalledServerData(KVDataWithSrcHost essentials) {
		this.query = essentials;
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

}
