import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;


public class ReassertKeystoDest extends RemoteMoveAndDeleteBulkCommand {

	KVDataReplicaBulkReqNoReply bulkNreply = null;
	public ReassertKeystoDest(TableEntry destHostEntry, KVStore kvStore,
			GossipTransmitter txObj, KVData[] data) {
		super(destHostEntry, kvStore, txObj, data);
		bulkNreply = new KVDataReplicaBulkReqNoReply(data);
	}
	
	public void execute() {
		String[] dataItems = destHostEntry.id.split("___");
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName(dataItems[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		int port = Integer.parseInt(dataItems[1]);
		
		//To send my keys if necessary to the new guy
		if (data == null) {
			return;
		}
		
		Gson gson = new Gson();
		MarshalledServerData mR = new MarshalledServerData(bulkNreply);
		String tx = gson.toJson(mR);
		// DEBUG System.out.println("Reassert keys " + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}
}
