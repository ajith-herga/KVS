import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;


public class IndirectLocalCommand extends PrimaryReplicaCommand {

	GossipTransmitter txObj = null;
	TableEntry destEntry = null;

	IndirectLocalCommand(KVDataWithSrcHost kvSrc, KVStore kvStore,
			GossipTransmitter txObj,
			ConcurrentHashMap<String,TableEntry> membTable,
			TableEntry selfEntry, List<ICommand> replicaCommands) {
		super(kvSrc.data, kvStore, txObj, membTable, selfEntry, replicaCommands);
		this.destEntry = kvSrc.srcHostEntry;
		this.txObj = txObj;
	}

	@Override
	public void callback(KVData cR) {
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
		MarshalledServerData mR = new MarshalledServerData(cR);
		String tx = gson.toJson(mR);
		System.out.println("IndirectLcommand: Callback" + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}

}
