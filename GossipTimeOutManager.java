import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class GossipTimeOutManager extends TimerTask{

	Timer timeoutTimer = null;
    ConcurrentHashMap<String, TableEntry> membTable = null;
	BufferedWriter bw = null;
	TableEntry selfEntry = null;
	GossipReceiver rxObj = null;

	public GossipTimeOutManager(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, TableEntry selfEntry, GossipReceiver rxObj) {
		this.membTable = membTable;
		this.bw = bw;
		this.selfEntry = selfEntry;
		this.rxObj = rxObj;
	}


	public void start() {
		// TODO Run this every some other time
		timeoutTimer = new Timer();
		timeoutTimer.schedule(this, 0, Constants.TIMER_PERIOD);
	}

	@Override
	public void run() {
		//lock.lock();
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		List<String> toBeDeleted = new ArrayList<String>();
		TableEntry[] sendnewReplicas = null;
		TableEntry[] sendoldReplicas = null;
		KVData[] leavekeys = null;
		KVData[] mykeys = null;
		//cBand.calcBand(currentTime);
		
		Iterator<TableEntry> iterator = membTable.values().iterator();
		while(iterator.hasNext()) {
			TableEntry entry = iterator.next();
			if(entry.equals(selfEntry)){
				continue;
			}
			if (entry.timerCheckNoChange(currentTime)) {
				sendoldReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
				leavekeys = rxObj.kvStore.getKVDataForMachine(entry, KVCommands.INSERTKV);
			}
			int k = entry.timerCheck(currentTime);
			if(k == 1){
				toBeDeleted.add(entry.id);
			} else if (k == 0) { 
				sendnewReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
    			rxObj.sendKeysToNewReplica(sendnewReplicas, sendoldReplicas);
    			mykeys = rxObj.kvStore.getKVDataForMachine(selfEntry, KVCommands.INSERTKV);
    			/* DEBUG System.out.println("My keys");
    			if (mykeys != null) {
	    			for (KVData temp1: mykeys) {
	    				System.out.println(temp1);
	    			}
    			}
    			System.out.println("Leave keys");
    			if (leavekeys != null) {
	    			for (KVData temp1: leavekeys) {
	    				System.out.println(temp1);
	    			}
    			} */
    			if (rxObj.supersetKeys(mykeys,leavekeys) && sendnewReplicas != null) {
    				ReassertKeystoDest resert = new ReassertKeystoDest(sendnewReplicas[0], rxObj.kvStore, rxObj.txObj, leavekeys);
    				resert.execute();
    				resert = new ReassertKeystoDest(sendnewReplicas[1], rxObj.kvStore, rxObj.txObj, leavekeys);
    				resert.execute();
    			}
    			try {
					bw.write(entry.id + ": Failed at " + new Date(currentTime));
					bw.newLine();
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(String id: toBeDeleted){
			membTable.remove(id);
		}
		//lock.unlock();
	}
}