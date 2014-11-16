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
	private long eventQueueCounter = 0;
	private PriorityQueue<Event> eventPriorityQueue = new PriorityQueue<Event>();

	public Map<Integer, UserClientThread> getClientConnections() {
		return clientConnections;
	}

	public void setClientConnections(
			Map<Integer, UserClientThread> clientConnections) {
		this.clientConnections = clientConnections;
	}

	private EventSinkThread eventSinkThread;

	/**
	 * CTOR is trying to read the port configuration from system properties
	 * before using the defaults
	 */
	public SocketServer() {
		clientListenerPort = Integer.parseInt(System.getProperty(
				Properties.clientListenerPortPropertyName,
				Properties.clientListenerPortDefault));
		eventListenerPort = Integer.parseInt(System.getProperty(
				Properties.eventListenerPortPropertyName,
				Properties.eventListenerPortDefault));
	}

	/**
	 * open the event sink socket in a separate thread and start it
	 */
	private void startEventConnection() {
		EventSinkThread eventSinkThread = new EventSinkThread(this);
		logger.debug("opening port " + eventListenerPort);
		eventSinkThread.openEventSocket(eventListenerPort);
		eventSinkThread.start();
	}

	/**
	 * we open a server socket and wait for the clients to connect. when that
	 * happens we pass the client socket to a separate client thread and start
	 * it to connect to it.
	 * 
	 * @return
	 * @throws IOException
	 */
	private ServerSocket listenForConnectingClients() throws IOException {
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

	/**
	 * register our client in the client connections map using the id as key
	 * 
	 * @see followermaze.server.RegisterCallback#registerClient(followermaze.server.UserClientThread)
	 */
	public void registerClient(UserClientThread userClientThread) {
		logger.info("trying to register client " + userClientThread.getId());
		synchronized (clientConnections) {
			PipedOutputStream eventStream = new PipedOutputStream();
			userClientThread.setEventStream(eventStream);
			clientConnections.put(userClientThread.getClientId(), userClientThread);
			logger.info("SSregistered client " + userClientThread.getId());
		}
	}

	public void registerEventSource(EventSinkThread eventSinkThread) {
		this.eventSinkThread = eventSinkThread;
	}

	/**
	 * this is called from the event listener/sink thread passing the event and
	 * the global event counter. <br>
	 * After placing the event in the orderedEventQueue, this handler checks if
	 * the queue head matches our event number. This is where we would introduce
	 * a time stamp and a timer to carry on if a message got missing.
	 * 
	 * @see followermaze.server.RegisterCallback#handleEvent(java.lang.String, int)
	 */
	public void handleEvent(String eventWord, int eventCounter)
			throws IOException {
		Event event = new Event(eventWord);
		logger.debug("eventNo " + eventCounter + " payload " + eventWord.toString());
		UserClientThread fromUCT = clientConnections.get(event.getFromUserId());
		UserClientThread toUCT = clientConnections.get(event.getToUserId());

		// synchronize on the priority queue to be thread safe (as long we only
		// call this from a single event sink thread this is not necessary)
		// synchronized (eventPriorityQueue) {
			eventPriorityQueue.add(event);
			Event queueHead = eventPriorityQueue.peek();

			// when we have the matching event in our queue we process it and
			// try also with the directly following seq nos
			while (queueHead != null
					&& queueHead.getSequenceNo() == eventQueueCounter) {
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
						logger.error("missing client Id for message "
								+ eventWord);
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
				// remove the head from the queue and peek at next one
				eventPriorityQueue.remove();
				queueHead = eventPriorityQueue.peek();
				eventQueueCounter++; // event processed, deal with next one
			}
	//	}   // synchronized priorityQueue
		return; // wait for next event to arrive
	}

	/**
	 * the main method starts the event listener and starts listening for
	 * clients after socket cleanup is attempted when leaving the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SocketServer server = new SocketServer();
		server.startEventConnection();
		try {
			server.listenForConnectingClients();
		} catch (IOException e) {
			logger.error("SS2 problem getting client connections", e);
		} finally {
			// close the sockets when we terminate
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
