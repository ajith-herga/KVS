import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class HashUtility {
	public static TableEntry findMachineForKey(ConcurrentHashMap<String,TableEntry> membTable, String keyHash) {
		List<TableEntry> machines = new LinkedList<TableEntry>(membTable.values());
		List<TableEntry> machinesAlive = new LinkedList<TableEntry>();
		for (TableEntry tE: machines) {
			if (tE.hasFailed)
				continue;
			machinesAlive.add(tE);
		}		
		Collections.sort(machinesAlive);
		for (TableEntry tE: machinesAlive) {
			if (tE.hashString.compareTo(keyHash) > 0) {
				return tE;
			}
		}
		return machinesAlive.get(0);
	}
	
	public static TableEntry findPreviousMachine(ConcurrentHashMap<String,TableEntry> membTable, String keyHash) {
		List<TableEntry> machines = new LinkedList<TableEntry>(membTable.values());
		List<TableEntry> machinesAlive = new LinkedList<TableEntry>();
		for (TableEntry tE: machines) {
			if (tE.hasFailed)
				continue;
			machinesAlive.add(tE);
		}		
		Collections.sort(machinesAlive);
		TableEntry prev = null;
		for (TableEntry tE: machinesAlive) {
			if (tE.hashString.compareTo(keyHash) == 0) {
				if (prev != null)
					return prev;
				else
					return machinesAlive.get(machinesAlive.size() - 1);
			}
			prev = tE;
		}
		return machinesAlive.get(0);
	}

	public static TableEntry[] findReplicaforMachine(ConcurrentHashMap<String,TableEntry> membTable, String keyHash) {
		List<TableEntry> machines = new LinkedList<TableEntry>(membTable.values());
		List<TableEntry> machinesAlive = new LinkedList<TableEntry>();
		for (TableEntry tE: machines) {
			if (tE.hasFailed)
				continue;
			machinesAlive.add(tE);
		}
		Collections.sort(machinesAlive);
		boolean found = false, flag = false;
		TableEntry[] returnReplicas = new TableEntry[2];
		for (TableEntry tE: machinesAlive) {
			if (tE.hashString.compareTo(keyHash) > 0 && found != true) {
				returnReplicas[0] = tE;
				found = true;
				continue;
			} else if (found == true) {
				returnReplicas[1] = tE;
				flag = true;
				break;
			}
		}
		if (machinesAlive.size() > 2) {
			if (returnReplicas[0] == null) {
				returnReplicas[0] = machinesAlive.get(0);
				returnReplicas[1] = machinesAlive.get(1);
				flag = true;
			} else if (returnReplicas[1] == null) {
				returnReplicas[1] = machinesAlive.get(0);
				flag = true;
			}
		}
		//Return only if you found two replicas, else we are not ready to do replication
		// yet.
		if (flag) {
			return returnReplicas;
		} else {
			return null;
		}
	}
}
