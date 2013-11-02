import java.util.Collections;
import java.util.List;


public class HashUtility {
	public static TableEntry findMachineForKey(List<TableEntry> machines, String keyString) {
		Collections.sort(machines);
		for (TableEntry tE: machines) {
			if (tE.hashString.compareTo(keyString) >= 0) {
				return tE;
			}
		}
		return machines.get(0);
	}
}
