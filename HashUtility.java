import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class HashUtility {
	public static TableEntry findMachineForKey(ConcurrentHashMap<String,TableEntry> membTable, String keyHash) {
		List<TableEntry> machines = new LinkedList<TableEntry>(membTable.values());
		Collections.sort(machines);
		for (TableEntry tE: machines) {
			if (tE.hashString.compareTo(keyHash) > 0) {
				return tE;
			}
		}
		return machines.get(0);
	}
}
