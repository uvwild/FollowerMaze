package followermaze.server;

import java.io.IOException;

public interface RegisterCallback {
	void registerClient(UserClientThread userClientThread);

	void registerEventSource(EventSinkThread eventSinkThread);

	/**
	 * this method is called by the event client thread on the event server and does all the magic
	 * of event handling by parsing the event and calling the appropriate action
	 * 
	 * @param event
	 * @param eventCounter
	 * @throws IOException
	 */
	void handleEvent(String event, int eventCounter) throws IOException;
}
