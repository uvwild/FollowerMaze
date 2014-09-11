package followermaze.server;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

/**
 * The {@link SocketServer}is the most minimal and intuitive implementation for
 * this task. A basic thread per connection model has been implemented. For each
 * connecting client a {@link UserClientThread} is created and placed in the
 * clientConnections HashMap. The events are read using the
 * {@link EventSinkThread} and written into a {@link PriorityQueue}. If there
 * are writable events they are written into the {@link PipedOutputStream} of
 * the corresponding {@link UserClientThread}.
 * 
 * A NIO based approach with a thread pool handling the events would the be more
 * efficient design.
 * 
 * @author uv.wildner simple server socket
 */
class SocketServer implements RegisterCallback {
	static Logger logger = Logger.getLogger(SocketServer.class);

	private int clientListenerPort;
	private int eventListenerPort;
	private Map<Integer, UserClientThread> clientConnections = new HashMap<Integer, UserClientThread>();
	private PriorityQueue<Event> eventQueue;

	public Map<Integer, UserClientThread> getClientConnections() {
		return clientConnections;
	}

	public void setClientConnections(
			Map<Integer, UserClientThread> clientConnections) {
		this.clientConnections = clientConnections;
	}

	private EventSinkThread eventSinkThread;

	/**
	 * CTOR reading the ports from system properties
	 */
	public SocketServer() {
		clientListenerPort = Integer.parseInt(System.getProperty(
				Properties.clientListenerPort,
				Properties.clientListenerPortDefault));
		eventListenerPort = Integer.parseInt(System.getProperty(
				Properties.eventListenerPort,
				Properties.eventListenerPortDefault));
		eventQueue = new PriorityQueue<Event>();
	}

	private void startEventConnection() {
		EventSinkThread eventSinkThread = new EventSinkThread(this);
		eventSinkThread.openEventSocket(eventListenerPort);
		eventSinkThread.start();
	}

	private ServerSocket waitForClientConnection() throws IOException {
		logger.info("listening on port " + clientListenerPort);
		ServerSocket clientListeningSocket = new ServerSocket(
				clientListenerPort);
		try {
			while (!clientListeningSocket.isClosed()) {
				Socket clientSocket = clientListeningSocket.accept();
				logger.info("connected to user Client on remote port "
						+ clientSocket.getPort());
				UserClientThread uct = new UserClientThread(clientSocket, this);
				uct.start();
			}
		} catch (Exception e) {
			logger.error("SS1 Exception when opening socket " + e.toString(), e);
		} finally {
			clientListeningSocket.close();
		}
		return null;
	}

	public void registerClient(UserClientThread userClientThread) {
		logger.info("trying to register client " + userClientThread.getId());
		synchronized (clientConnections) {
			PipedOutputStream eventStream = new PipedOutputStream();
			userClientThread.setEventStream(eventStream);
			clientConnections.put(userClientThread.getClientId(),
					userClientThread);
			logger.info("SSregistered client " + userClientThread.getId());
		}
	}

	public void registerEventSource(EventSinkThread eventSinkThread) {
		this.eventSinkThread = eventSinkThread;
	}

	public void handleEvent(String eventWord, int eventCounter)
			throws IOException {
		Event event = new Event(eventWord);
		eventQueue.add(event);
		logger.debug("eventNo " + eventCounter + " payload "
				+ eventWord.toString());
		UserClientThread fromUCT = clientConnections.get(event.getFromUserId());
		UserClientThread toUCT = clientConnections.get(event.getToUserId());

		Event queueHead = eventQueue.peek();
		// send all the event we can
		while (queueHead.getSequenceNo() == eventCounter) {
			switch (queueHead.getType()) {
			case BROADCAST: {
				for (UserClientThread uct : clientConnections.values()) {
					uct.getEventStream().write(eventWord.getBytes());
					uct.getEventStream().flush();
				}
				break;
			}
			case FOLLOW:
				if (fromUCT != null && toUCT != null)
					fromUCT.getFollowers().add(toUCT);
				// fall through
			case PRIVATEMSG:
				if (toUCT != null) {
					toUCT.getEventStream().write(eventWord.getBytes());
					toUCT.getEventStream().flush();
				} else {
					logger.error("missing client Id for message " + eventWord);
					return;
				}
				break;
			case UNFOLLOW:
				if (fromUCT != null && toUCT != null) {
					fromUCT.getFollowers().remove(toUCT);
				} else {
					logger.error("missing client Id for message " + eventWord);
					return;
				}
				break;
			case STATUSUPDATE:
				if (fromUCT != null) {
					for (UserClientThread uct : fromUCT.getFollowers()) {
						uct.getEventStream().write(eventWord.getBytes());
						uct.getEventStream().flush();
					}
				}
				break;
			default:
				logger.error("unknown eventype " + eventWord);
			}
			eventCounter++; // event processed, deal with next one
		}
		return; // wait for next event to arrive
	}

	public static void main(String[] args) {
		SocketServer server = new SocketServer();
		server.startEventConnection();
		try {
			server.waitForClientConnection();
		} catch (IOException e) {
			logger.error("SS2 problem getting client connections", e);
		} finally {
			try {
				server.eventSinkThread.getEventSocket().close();
				for (UserClientThread uct : server.getClientConnections()
						.values()) {
					uct.getClientSocket().close();
				}
			} catch (IOException e) {
				logger.error("SS3 problem closing sockets", e);
			}
		}
	}
}
