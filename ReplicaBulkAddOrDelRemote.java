import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.google.gson.Gson;


public class ReplicaBulkAddOrDelRemote implements ICommand {
	KVData[] data = null;
	TableEntry destHostEntry;
	TableEntry selfEntry;
	GossipTransmitter txObj = null;
	KVDataReplicaBulkReq dataBulk = null;
	ICommand parent = null;
	
	public ReplicaBulkAddOrDelRemote (TableEntry destHostEntry, TableEntry selfEntry,
			GossipTransmitter txObj, KVData[] data, ICommand parent) {
		this.destHostEntry = destHostEntry;
		this.txObj = txObj;
		this.selfEntry = selfEntry;
		this.data = data;
	    this.parent = parent;
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
		
		dataBulk = new KVDataReplicaBulkReq(data, selfEntry);
		Gson gson = new Gson();
		MarshalledServerData mR = new MarshalledServerData(dataBulk);
		String tx = gson.toJson(mR);
		System.out.println("ReplicaBulkAddorDel" + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
		// Remove Keys from replica: req sent waiting for callack
		// Remove keys locally? Not yet.  I have t o send these to new guy
	}

	public void callback(KVData cR) {
		System.out.println("Callback ReplicaBulk for sent " + data);
		if (parent != null) {
		    System.out.println("ReplicaBulk parent callback");
			parent.callback(cR);
		}
	}

	@Override
	public long getId() {
		return dataBulk.milisPrimary;
	}

}
