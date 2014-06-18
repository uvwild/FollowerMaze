package followermaze.server;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author uv.wildner simple server socket
 */
class SocketServer implements RegisterCallback {
	private int								clientListenerPort;
	private int								eventListenerPort;
	private int								eventCounter		= 0;
	private Map<Integer, UserClientThread>	clientConnections	= new HashMap<Integer, UserClientThread>();

	public Map<Integer, UserClientThread> getClientConnections() {
		return clientConnections;
	}

	public void setClientConnections(Map<Integer, UserClientThread> clientConnections) {
		this.clientConnections = clientConnections;
	}

	private EventSinkThread	eventSinkThread;

	public SocketServer() {
		super();
		clientListenerPort = Integer.parseInt(System.getProperty(	Properties.clientListenerPort,
																	Properties.clientListenerPortDefault));
		eventListenerPort = Integer.parseInt(System.getProperty(Properties.eventListenerPort,
																Properties.eventListenerPortDefault));
		SocketChannel sc = null;

	}

	private void startEventConnection() {
		EventSinkThread eventSinkThread = new EventSinkThread(this);
		eventSinkThread.openEventSocket(eventListenerPort);
		eventSinkThread.start();
	}

	private ServerSocket waitForClientConnection() throws IOException {
		System.out.format("listening on port %d\n", clientListenerPort);
		ServerSocket clientListeningSocket = new ServerSocket(clientListenerPort);
		try {
			while (!clientListeningSocket.isClosed()) {
				Socket clientSocket = clientListeningSocket.accept();
				System.out.format("connected to user Client on remote port %s\n", clientSocket.getPort());
				UserClientThread uct = new UserClientThread(clientSocket, this);
				uct.start();
			}
			// System.out.format("connected on {}", eventListenerPort);
		} catch (Exception e) {
			System.out.format("Exception when opening socket %s", e.toString());
		} finally {
			clientListeningSocket.close();
		}
		return null;
	}

	@Override
	public void registerClient(UserClientThread userClientThread) {
		System.out.format("trying to register client %d.....", userClientThread.getId());
		synchronized (clientConnections) {
			PipedOutputStream eventStream = new PipedOutputStream();
			userClientThread.setEventStream(eventStream);
			clientConnections.put(userClientThread.getClientId(), userClientThread);
			System.out.format("registered client %d\n", userClientThread.getId());
		}
	}

	@Override
	public void registerEventSource(EventSinkThread eventSinkThread) {
		this.eventSinkThread = eventSinkThread;
	}

	public void handleEvent(String event, int eventCounter) {
		Event eve = new Event(event);
		System.out.format("event %d: %s", eventCounter, event.toString());
		Integer toClientId = eve.getToUserId();
		if (toClientId != null && clientConnections.containsKey(eve.getSequenceNo())) {
			UserClientThread uct = clientConnections.get(toClientId);
			try {
				uct.getEventStream().write(event.getBytes());
				uct.getEventStream().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		SocketServer server = new SocketServer();
		server.startEventConnection();
		try {
			server.waitForClientConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				server.eventSinkThread.getEventSocket().close();
				for (UserClientThread uct : server.getClientConnections().values()) {
					uct.getClientSocket().close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
