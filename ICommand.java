
public interface ICommand {
	public void execute();
	public void callback(KVData cR);
	public long getId();
}
