package followermaze.server;

import java.io.IOException;

public interface RegisterCallback {
	void registerClient(UserClientThread userClientThread);

	void registerEventSource(EventSinkThread eventSinkThread);

	void handleEvent(String event, int eventCounter) throws IOException;
}
