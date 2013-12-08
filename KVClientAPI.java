import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class KVClientAPI {
    Requester requester = null;
    String hostName = null, portOne = null;
	Socket sock = null;
	KVClientResponse result = null;
    KVClientShowResponse showResult = null;
 
	public KVClientAPI(String hostName, String port) {
		
		portOne = port;
		this.hostName = hostName;
	}

	/* 
     * The requester thread gets the host/port information. 
     * thread tires to connect to the remote server.
     * TODO: allow to return right after command is sent: not req ACK, but windowing.
     * will have to have a command stack here too.
     */
	private class Requester extends Thread {
		KVData query;


        Requester(KVData query) {
            try {
                sock = new Socket(hostName, Integer.parseInt(portOne));
            } catch (UnknownHostException e) {
                System.out.println("Don't know about host: "+ hostName + " " + portOne);
                return;
            } catch (IOException e) {
                System.out.println("No socket for " + hostName + " " + portOne);
                return;
            }
             this.query = query;
             result = new KVClientResponse();
             showResult = new KVClientShowResponse();
             result.Status = true;
             showResult.Status = true;
        }

		@Override
		public void run() {
			//System.out.println("Client: Thread Start Send");
			PrintWriter out = null;
	        BufferedReader in = null;
	        String servLine = null;
	        if (sock == null) {
	        	return;
	        }
	        try {
	            out = new PrintWriter(sock.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                                        sock.getInputStream()));
	        } catch (IOException e) {
	            System.out.println("No Buffered I/O for " + hostName + " " + portOne);
	            return;
	        }

	        Gson gson = new Gson();
        	MarshalledClientData mcD = new MarshalledClientData(query);
    		String tx = gson.toJson(mcD);
    		System.out.println("KVClientAPI: Run" + tx);
    		out.println(tx);

	        try {

	        	if ((servLine = in.readLine()) != null) {
					Type dataType = new TypeToken<MarshalledClientData>(){}.getType();
					MarshalledClientData marshalledData = gson.fromJson(servLine,dataType);

					if (marshalledData.showData!=null) {
						if(marshalledData.showData.length == 0){
							showResult.Status = true;
							showResult.kV = new KeyAndValue[0];	
							return;
						}
					} else if ((marshalledData.data!=null && marshalledData.data.code==StatusCode.FAILURE) || marshalledData.data==null) {
						result.Status = false;
						return;
					}

					switch(query.command) {
						case LOOKUPKV:
							result.kV = new KeyAndValue(marshalledData.data.key.key, marshalledData.data.value);
							break;
						case DELETEKV:
						case MODIFYKV:
							result.kV = new KeyAndValue(marshalledData.data.key.key, marshalledData.data.value);
						case INSERTKV:
							result.kV = new KeyAndValue(marshalledData.data.key.key, marshalledData.data.value);
							break;
						case SHOWALL:
							KeyAndValue[] kVArray = new KeyAndValue[marshalledData.showData.length];
							int i = 0;
							for (KVData data: marshalledData.showData) {
								kVArray[i++] = new KeyAndValue(data.key.key, data.value);
							}
							showResult.kV = kVArray;
							break;
						default:
							result.Status = false;
					}
	        	}
	        } catch (IOException e) {
	            System.out.println("Couldnot read from Buffered I/O " + hostName + " " + portOne);
	            return;
	        }
			//System.out.println("Client: Thread Finish Send");
		}

	}

	public KVClientShowResponse showAll() {
		KVData data = new KVData(KVCommands.SHOWALL, null, null, System.currentTimeMillis(),StatusCode.FAILURE, 1);
		requester = new Requester(data);
		requester.start();
		joinThreads();
		return showResult;
	}

	public KVClientResponse insert(long key, Object Value, int level) {
	    KVData data = new KVData(KVCommands.INSERTKV, key, Value, System.currentTimeMillis(),StatusCode.FAILURE, level);
	    requester = new Requester(data);
		requester.start();
		joinThreads();
		return result;
	}
	
	public KVClientResponse modify(long key, Object Value, int level) {
	    KVData data = new KVData(KVCommands.MODIFYKV, key, Value, System.currentTimeMillis(),StatusCode.FAILURE, level);
	    requester = new Requester(data);
		requester.start();
		joinThreads();
		return result;
	}

	public KVClientResponse lookup(long key, int level) {
	    KVData data = new KVData(KVCommands.LOOKUPKV, key, null, System.currentTimeMillis(),StatusCode.FAILURE, level);
	    requester = new Requester(data);
		requester.start();
		joinThreads();
		return result;
	}

	public KVClientResponse delete(long key, int level) {
	    KVData data = new KVData(KVCommands.DELETEKV, key, null, System.currentTimeMillis(),StatusCode.FAILURE, level);
	    requester = new Requester(data);
		requester.start();
		joinThreads();
		return result;
	}

	public void joinThreads() {
		try {
			requester.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}