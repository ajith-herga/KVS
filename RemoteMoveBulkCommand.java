import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;


public class RemoteMoveBulkCommand implements ICommand{

	KVData[] data = null;
	TableEntry destHostEntry;
	GossipTransmitter txObj = null;
	KVStore kvStore = null;
	boolean leave = false;

	public RemoteMoveBulkCommand (TableEntry destHostEntry, GossipTransmitter txObj, KVStore kvStore, boolean leave) {
		this.destHostEntry = destHostEntry;
		this.txObj = txObj;
		this.kvStore = kvStore;
		this.leave = leave;
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
		if (leave) {
			data = kvStore.getAllKVData();
		} else {
			data = kvStore.getKVDataForMachine(destHostEntry);
		}
		if (data == null) {
			return;
		}
		
		Gson gson = new Gson();
		MarshalledServerData mR = new MarshalledServerData(data);
		String tx = gson.toJson(mR);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
		// Remove Keys from me I have sent these to new guy
		for (KVData each: data) {
			kvStore.removeKey(each.key);
		}
	}

	public void callback(KVData cR) {
		//do nothing
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
