package followermaze.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventEnumTest {

	@Test
	public void testCTOR() {
		String[] codes = { "B", "F", "P", "S", "U" };
		for (String code :codes) {
			EventEnum ee = EventEnum.fromString(code);
			assertEquals("Invalid enum", ee.name().charAt(0), code.charAt(0));
		}
	} 

}
