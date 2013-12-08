import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;

public class RemoteCommand implements ICommand {

	KVClientRequestServer.Worker sourceWorker;
	TableEntry destHostEntry;
	GossipTransmitter txObj = null;
	TableEntry selfEntry = null;
	KVDataWithSrcHost essentials = null;
	
	public RemoteCommand(KVClientRequestServer.Worker sW, TableEntry destHostEntry, GossipTransmitter txObj,
			      TableEntry selfEntry, KVData data) {
		this.sourceWorker = sW;
		this.destHostEntry = destHostEntry;
		this.txObj = txObj;
		this.selfEntry = selfEntry;
        this.essentials = new KVDataWithSrcHost(data, selfEntry);
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
		Gson gson = new Gson();
		MarshalledServerData mR = new MarshalledServerData(essentials);
		String tx = gson.toJson(mR);
		System.out.println("RemoteCommand Exec: " + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}

	public void callback(KVData cR) {
		Gson gson = new Gson();
		MarshalledClientData mcD = new MarshalledClientData(cR); 
		String tx = gson.toJson(mcD);
		//This is to say that we are done with the connection.
		System.out.println("RemoteCommand: Callback " + tx);
		sourceWorker.send(tx);
		sourceWorker.closeClient();
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return essentials.data.id;
	}
}
