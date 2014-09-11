package followermaze.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * @author uv.wildner The {@link UserClientThread} is the thread per client in
 *         the communication model. Upon successful connection this class
 *         registers itself with the {@link SocketServer}
 * 
 */
public class UserClientThread extends Thread implements
		Comparable<UserClientThread> {

	static Logger logger = Logger.getLogger(UserClientThread.class);

	private Socket clientSocket;

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	private BufferedReader socketIn;
	private BufferedReader eventIn;
	private PipedOutputStream eventStream;

	public PipedOutputStream getEventStream() {
		return eventStream;
	}

	public void setEventStream(PipedOutputStream eventStream) {
		this.eventStream = eventStream;
	}

	private Integer eventCounter = 0;
	RegisterCallback socketServer;

	private PrintWriter socketOut;
	private Integer clientId;
	private Set<UserClientThread> followers;

	public Integer getClientId() {
		return clientId;
	}

	public void setClientd(Integer id) {
		this.clientId = id;
	}

	public UserClientThread(Socket clientSocket, RegisterCallback socketServer) {
		super();
		this.clientSocket = clientSocket;
		this.socketServer = socketServer;
		followers = new TreeSet<UserClientThread>();

		try {
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		readClientId();
		socketServer.registerClient(this);
		try {
			eventIn = new BufferedReader(new InputStreamReader(
					new PipedInputStream(eventStream)));
		} catch (IOException e) {
			logger.error("UCT1 problem creating input pipe", e);
		}
		handleEvents();
	}

	private void handleEvents() {
		String event;
		try {
			while ((event = eventIn.readLine()) != null) {
				eventCounter++;
				socketOut.append(event);
			}
		} catch (IOException e) {
			logger.error("UCT2 problem reading event", e);
		}
	}

	/**
	 * read client Id from socket and send back to server to mark reception of
	 * events
	 */
	void readClientId() {
		try {
			clientId = Integer.valueOf(socketIn.readLine());
			logger.debug("read Client id on remote port "
					+ clientSocket.getPort() + " " + clientId);
		} catch (NumberFormatException e) {
			logger.error("UCT3 problem reading client id", e);
		} catch (IOException e) {
			logger.error("UCT4 problem reading client id", e);
		}
	}

	public Set<UserClientThread> getFollowers() {
		return followers;
	}

	public void setFollowers(Set<UserClientThread> followers) {
		this.followers = followers;
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(UserClientThread other) {
		return clientId.compareTo(other.getClientId());
	}
}
