package followermaze.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EventSinkThread extends Thread {

	public ServerSocket getEventSocket() {
		return eventSocket;
	}

	public void setEventSocket(ServerSocket eventSocket) {
		this.eventSocket = eventSocket;
	}

	private ServerSocket	eventSocket;
	private BufferedReader	eventIn;
	private PrintWriter		socketOut;
	private int				eventCounter	= 0;
	RegisterCallback		callback;

	public EventSinkThread(RegisterCallback callback) {
		super();
		this.callback = callback;
	}

	void openEventSocket(int eventListenerPort) {
		System.out.format("listening on port %d\n", eventListenerPort);
		try {
			eventSocket = new ServerSocket(eventListenerPort);
			Socket eventConnection = eventSocket.accept();
			// TODSO not sure if it is at all needed to open the stream ??
			socketOut = new PrintWriter(eventConnection.getOutputStream(), true);
			eventIn = new BufferedReader(new InputStreamReader(eventConnection.getInputStream()));
			System.out.format("connected eventstream on %d\n", eventListenerPort);
			callback.registerEventSource(this);
		} catch (Exception e) {
			System.out.format("Exception when opening socket %s", e.toString());
		}
	}

	@Override
	public void run() {
		readEvents();
	}

	private void readEvents() {
		String event;
		try {
			while ((event = eventIn.readLine()) != null) {
				System.out.format("read event %s\n", event);
				eventCounter++;
				callback.handleEvent(event, eventCounter);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
