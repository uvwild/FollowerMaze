package followermaze.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * @author uv.wildner this thread is the event sink and passes the events to the
 *         server for processing. Since there is only one event socket this
 *         could be also done within the {@link ServerSocket} class. However, it
 *         has been placed in a separate thread, permitting also additional
 *         sockets to be served.
 */
public class EventSinkThread extends Thread {
	static org.apache.log4j.Logger logger = Logger
			.getLogger(EventSinkThread.class);

	public ServerSocket getEventSocket() {
		return eventSocket;
	}

	public void setEventSocket(ServerSocket eventSocket) {
		this.eventSocket = eventSocket;
	}

	private ServerSocket eventSocket;
	private BufferedReader eventIn;
	private int eventCounter = 0;
	RegisterCallback callback;

	public EventSinkThread(RegisterCallback callback) {
		super();
		this.callback = callback;
	}

	void openEventSocket(int eventListenerPort) {
		logger.info("listening on port " + eventListenerPort);
		try {
			eventSocket = new ServerSocket(eventListenerPort);
			Socket eventConnection = eventSocket.accept();
			new PrintWriter(eventConnection.getOutputStream(), true);
			eventIn = new BufferedReader(new InputStreamReader(
					eventConnection.getInputStream()));
			logger.info("connected eventstream on " + eventListenerPort);
			callback.registerEventSource(this);
		} catch (Exception e) {
			logger.error("EST1 Exception when opening socket ", e);
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
				logger.debug("read event " + event);
				eventCounter++;
				callback.handleEvent(event, eventCounter);
			}
		} catch (IOException e) {
			logger.error("EST2 problem reading stream", e);
		}
	}
}
