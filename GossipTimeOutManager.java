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

	public GossipTimeOutManager(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, TableEntry selfEntry) {
		this.membTable = membTable;
		this.bw = bw;
		this.selfEntry = selfEntry;
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
		
		//cBand.calcBand(currentTime);
		
		Iterator<TableEntry> iterator = membTable.values().iterator();
		while(iterator.hasNext()) {
			TableEntry entry = iterator.next();
			if(entry.equals(selfEntry)){
				continue;
			}
			int k = entry.timerCheck(currentTime);
			if(k == 1){
				toBeDeleted.add(entry.id);
			} else if (k == 0) { 
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