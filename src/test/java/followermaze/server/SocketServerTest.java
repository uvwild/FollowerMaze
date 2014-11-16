/** 
 * 
 */
package followermaze.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * @author uv.wildner
 *
 */
public class SocketServerTest extends EventTestBase {

	SocketServer socketServer;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		socketServer = new  SocketServer();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		socketServer = null;
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#getClientConnections()}.
	 */
	@Test
	public void testGetClientConnections() {
//		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#setClientConnections(java.util.Map)}.
	 */
	@Test
	public void testSetClientConnections() {
//		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#SocketServer()}.
	 */
	@Test
	public void testSocketServer() {
//		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#registerClient(followermaze.server.UserClientThread)}.
	 * @throws IOException 
	 */
	@Test
	public void testRegisterClient() throws IOException {
        Socket socketMock = mock(Socket.class);
        OutputStream istreamMock = mock(OutputStream.class); 
        InputStream ostreamMock = mock(InputStream.class);	 
        when(socketMock.getOutputStream()).thenReturn(istreamMock);
        when(socketMock.getInputStream()).thenReturn(ostreamMock);
		UserClientThread uct = new UserClientThread(socketMock, socketServer);
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#registerEventSource(followermaze.server.EventSinkThread)}.
	 */
	@Test
	public void testRegisterEventSource() {
//		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link followermaze.server.SocketServer#handleEvent(java.lang.String, int)}.
	 */
	@Test
	public void testHandleEvent() {
		for (Event evt : getEventList()) {
			int i=0;
			try {
				socketServer.handleEvent(evt.getEventWord(), i++);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("caught exception when handling Event" + e.toString());
			}
		}
	}

}
