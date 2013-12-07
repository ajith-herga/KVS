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
		System.out.println("findPreviousMachine: Absent Input Current Machine in memTable");
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
		boolean found = false;
		TableEntry[] returnReplicas = new TableEntry[2];
		for (TableEntry tE: machinesAlive) {
			if (tE.hashString.compareTo(keyHash) < 0 && found != true) {
				returnReplicas[0] = tE;
				found = true;
				continue;
			} else if (found == true) {
				returnReplicas[1] = tE;
				break;
			}
		}
		//Return only if you found two replicas, else we are not ready to do replication
		// yet.
		if (returnReplicas.length == 2) {
			return returnReplicas;
		} else {
			return null;
		}
	}
}
