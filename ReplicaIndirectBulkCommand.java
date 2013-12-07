import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;


public class ReplicaIndirectBulkCommand implements ICommand{
	
	KVStore kvStore = null;
	GossipTransmitter txObj = null;
	TableEntry destEntry = null;
    KVDataReplicaBulkReq replicaBulkReq = null;
    KVDataReplicaBulkReply replicaBulkReply = null;
    
	public ReplicaIndirectBulkCommand(KVDataReplicaBulkReq replicaBulkReq,
			KVStore kvStore, GossipTransmitter txObj) {
		this.kvStore = kvStore;
		this.txObj = txObj;
		this.destEntry = replicaBulkReq.srcHostEntry;
		this.replicaBulkReq = replicaBulkReq;
		this.replicaBulkReply = new KVDataReplicaBulkReply(replicaBulkReq.milisPrimary);
	}

	@Override
	public void execute() {
		for (KVData data: replicaBulkReq.data) {
			ReplicaIndirectLocalCommandNoCallback inDir = new ReplicaIndirectLocalCommandNoCallback(data, kvStore);
			inDir.execute();
		}
		callback(null);
	}

	@Override
	public void callback(KVData cR) {
		//Note cR is null!
		String[] dataItems = destEntry.id.split("___");
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName(dataItems[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		int port = Integer.parseInt(dataItems[1]);
		Gson gson = new Gson();
		MarshalledServerData mR = new MarshalledServerData(replicaBulkReply);
		String tx = gson.toJson(mR);
		System.out.println("ReplicaIndirectBulk Callback: " + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
