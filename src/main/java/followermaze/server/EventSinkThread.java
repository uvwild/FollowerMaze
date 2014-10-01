package followermaze.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * @author uv.wildner <br>
 *         This thread is used for the event sink and passes the events to the server for
 *         processing. Since there is only one event socket this could be also done within the
 *         {@link ServerSocket} class. However, it has been placed in a separate thread, permitting
 *         also additional sockets to be served.
 */
public class EventSinkThread extends Thread {

	static org.apache.log4j.Logger logger = Logger.getLogger(EventSinkThread.class);

	private ServerSocket eventSocket;

	public ServerSocket getEventSocket() {
		return eventSocket;
	}

	public void setEventSocket(ServerSocket eventSocket) {
		this.eventSocket = eventSocket;
	}

	private BufferedReader eventIn;
	private int eventCounter;
	RegisterCallback callback;

	public EventSinkThread(RegisterCallback callback) {
		super();
		this.callback = callback;
	}

	/**
	 * given the port we open a buffered reader for the input of the event connection and a
	 * printwriter for the output after we are using a callback on the socket server to register the
	 * event source (leaving the option to add more)
	 * 
	 * @param eventListenerPort
	 */
	void openEventSocket(int eventListenerPort) {
		try {
			eventSocket = new ServerSocket(eventListenerPort);
			logger.info("listening for events on port " + eventListenerPort);
			Socket eventConnection = eventSocket.accept();
			// new PrintWriter(eventConnection.getOutputStream(), true);
			eventIn = new BufferedReader(new InputStreamReader(eventConnection.getInputStream()));
			logger.info("connected eventstream on " + eventListenerPort);
			callback.registerEventSource(this);
			eventCounter = 0; // reset
		} catch (Exception e) {
			logger.error("EST1 Exception when opening socket ", e);
		}
	}

	@Override
	public void run() {
		try {
			readEvents();
		} finally {
			try {
				eventSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				eventIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * read the buffered input stream into strings (hoping thats complete messages) and callback the
	 * socket server to deal with them
	 */
	private void readEvents() {
		String event;
		try {
			while ((event = eventIn.readLine()) != null) {
				logger.debug("read event " + event);
				eventCounter++; //
				callback.handleEvent(event, eventCounter);
			}
		} catch (IOException e) {
			logger.error("EST2 problem reading stream", e);
		}
	}
}
