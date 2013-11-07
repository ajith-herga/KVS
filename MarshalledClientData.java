
public class MarshalledClientData {

    KVData data = null;
    KVData[] showData = null;

    public MarshalledClientData(KVData cR) {
		this.data = cR;
	}

	public MarshalledClientData(KVData[] cRs) {
		this.showData = cRs;
	}

}
