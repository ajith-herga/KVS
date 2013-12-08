import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;


public class ReplicaIndirectCommand extends LocalCommand {

	GossipTransmitter txObj = null;
	TableEntry destEntry = null;
	KVDataReplicaReply kvReplReply = null;

	ReplicaIndirectCommand(KVDataReplicaReq kvReq, KVStore kvStore,
			GossipTransmitter txObj) {
		super(kvReq.data, kvStore);
		this.destEntry = kvReq.srcHostEntry;
		this.txObj = txObj;
		kvReplReply = new KVDataReplicaReply(kvReq);
	}

	public void callback(KVData unR) {
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
		MarshalledServerData mR = new MarshalledServerData(kvReplReply);
		String tx = gson.toJson(mR);
		// DEBUG System.out.println("ReplicaIndirect Callback: " + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}
}
