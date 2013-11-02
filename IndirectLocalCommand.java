import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;


public class IndirectLocalCommand extends LocalCommand {

	GossipTransmitter txObj = null;
	TableEntry destEntry = null;

	IndirectLocalCommand(KVDataWithSrcHost kvSrc, KVStore kvStore, GossipTransmitter txObj) {
		super(kvSrc.data, kvStore);
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
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return data.id;
	}

}
