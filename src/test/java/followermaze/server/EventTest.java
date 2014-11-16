package followermaze.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EventTest extends EventTestBase {

	@Test
	public void testEventCreation() {
		for (int i=0; i< seqno.length; i++) {
			String evtStr = seqno[i].toString()+separator+
					code[i]+separator+
					(from[i]!=null?from[i]:"")+separator+
					(to[i]!=null?to[i]:"");
			Event e = new Event(evtStr);
			assertEquals("SequenceNo wrong", seqno[i], e.getSequenceNo());
			assertEquals("Code wrong", EventEnum.fromString(code[i]), e.getType());
			assertEquals("from wrong", seqno[i], e.getSequenceNo());
			assertEquals("to wrong", seqno[i], e.getSequenceNo());
		}
	}

}
