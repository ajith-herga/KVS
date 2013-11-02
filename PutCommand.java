
public class PutCommand extends RemoteCommand {

    
	public PutCommand(KVClientRequestServer.Worker sW, TableEntry destHostEntry, GossipTransmitter txObj,
			TableEntry selfEntry, KVData data) {
		super(sW, destHostEntry, txObj, selfEntry, data);
	}

}
