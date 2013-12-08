import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;


public class RemoteReplicaCommand implements ICommand {

	TableEntry destHostEntry = null;
	GossipTransmitter txObj = null;
	TableEntry selfEntry = null;
	KVDataReplicaReq essentials = null;
	PrimaryReplicaCommand originatorLocalCommand = null;
	
	public RemoteReplicaCommand(KVData data, TableEntry destHostEntry, GossipTransmitter txObj,
			      TableEntry selfEntry, PrimaryReplicaCommand originatorLocalCommand) {
		this.destHostEntry = destHostEntry;
		this.txObj = txObj;
		this.selfEntry = selfEntry;
        this.essentials = new KVDataReplicaReq(data, selfEntry);
        this.originatorLocalCommand = originatorLocalCommand;
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
		//System.out.println("ReplicaCommandExecute: " + tx);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		txObj.send(sendpacket);
	}

	public void callback(KVData cR) {
		// Deleted send and close connection at local
		// May need a periodic timer that removes the entries in this and other list:
		// Processes may die and may not reply.. can call callback with failed error.
		if (originatorLocalCommand == null) {
			return;
		}
	    //System.out.println("RemoteReplicaCommand Callback decrement " + originatorLocalCommand.expectRepliesRx + " ID:" + cR.id);
		if (originatorLocalCommand.quorumLevel != 0) {
			originatorLocalCommand.quorumLevel--;
			if (originatorLocalCommand.quorumLevel == 0) {
				originatorLocalCommand.callback(cR);
			}
		}
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		//System.out.println("Replica Command id return" + essentials.milisPrimary);
		return essentials.milisPrimary;
	}

}
