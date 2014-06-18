package followermaze.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class UserClientThread extends Thread {

	private Socket	clientSocket;

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	private BufferedReader		socketIn;
	private BufferedReader		eventIn;
	private PipedOutputStream	eventStream;

	public PipedOutputStream getEventStream() {
		return eventStream;
	}

	public void setEventStream(PipedOutputStream eventStream) {
		this.eventStream = eventStream;
	}

	private int			eventCounter	= 0;
	RegisterCallback	callback;

	private PrintWriter	socketOut;
	private Integer		clientId;

	public Integer getClientId() {
		return clientId;
	}

	public void setClientd(Integer id) {
		this.clientId = id;
	}

	public UserClientThread(Socket clientSocket, RegisterCallback callback) {
		super();
		this.clientSocket = clientSocket;
		this.callback = callback;
		try {
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		readClientId();
		callback.registerClient(this);
		try {
			eventIn = new BufferedReader(new InputStreamReader(new PipedInputStream(eventStream)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * read client Id from socket and send back to server to mark reception of events
	 */
	void readClientId() {
		try {
			clientId = Integer.valueOf(socketIn.readLine());
			System.out.format("read Client id on remote port %d %d\n", clientSocket.getPort(), clientId);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
