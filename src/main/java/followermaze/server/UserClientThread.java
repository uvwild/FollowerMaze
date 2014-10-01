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
 * @author uv.wildner The {@link UserClientThread} is the thread per client in the communication
 *         model. Upon successful connection this class registers itself with the
 *         {@link SocketServer}
 * 
 */
public class UserClientThread extends Thread implements Comparable<UserClientThread> {

	static Logger logger = Logger.getLogger(UserClientThread.class);

	private Integer clientId;

	private Socket clientSocket;

	private Integer eventCounter = 0;

	private BufferedReader eventIn;

	private PipedOutputStream eventStream;
	private Set<UserClientThread> followers = new TreeSet<UserClientThread>();
	private BufferedReader socketIn;

	private PrintWriter socketOut;

	RegisterCallback socketServer;

	/**
	 * CTOR takes the socket from the socket server after a client has connected together with a
	 * callback handle. A buffered reader for the input and a printwriter for the output stream are
	 * created.
	 * 
	 * @param clientSocket
	 * @param socketServer
	 */
	public UserClientThread(Socket clientSocket, RegisterCallback socketServer) {
		super();
		this.clientSocket = clientSocket;
		this.socketServer = socketServer;

		try {
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(UserClientThread other) {
		return clientId.compareTo(other.getClientId());
	}

	public Integer getClientId() {
		return clientId;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public PipedOutputStream getEventStream() {
		return eventStream;
	}

	public Set<UserClientThread> getFollowers() {
		return followers;
	}

	/**
	 * the loop listening for events on the buffered event input stream we wrapped around the event
	 * pipe written by the socket server. we receive here the direct messages from another client to
	 * this client which are just passed on by the socket server and simply echoed to this client in
	 * this method.
	 */
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
	 * read client Id from socket to confirm connection to the client
	 */
	void readClientId() {
		try {
			clientId = Integer.valueOf(socketIn.readLine());
			logger.debug("read Client id on remote port " + clientSocket.getPort() + " " + clientId);
		} catch (NumberFormatException e) {
			logger.error("UCT3 problem reading client id", e);
		} catch (IOException e) {
			logger.error("UCT4 problem reading client id", e);
		}
	}

	/**
	 * when the client thread is started it registers with the socket server. <br>
	 * the socket server writes messages to an event pipe which we connect to an input stream which
	 * we wrap in a buffered reader. The client thread listens on the pipe for messages and passes
	 * them on the client socket.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		readClientId();
		socketServer.registerClient(this);
		try {
			eventIn = new BufferedReader(new InputStreamReader(new PipedInputStream(eventStream)));
		} catch (IOException e) {
			logger.error("UCT1 problem creating input pipe", e);
		}
		handleEvents();
	}

	public void setClientd(Integer id) {
		this.clientId = id;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void setEventStream(PipedOutputStream eventStream) {
		this.eventStream = eventStream;
	}

	public void setFollowers(Set<UserClientThread> followers) {
		this.followers = followers;
	}
}
