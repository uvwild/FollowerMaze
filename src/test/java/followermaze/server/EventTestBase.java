package followermaze.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author uv
|666|F|60|50 | 666       | Follow       | 60           | 50         |
|1|U|12|9    | 1         | Unfollow     | 12           | 9          |
|542532|B    | 542532    | Broadcast    | -            | -          |
|43|P|32|56  | 43        | Private Msg  | 32           | 56         |
|634|S|32    | 634       | Status Update| 32           | -          |
 */
public class EventTestBase {

	Character separator = '|';
	Long[] seqno = { 666L, 1L, 542532L, 43L, 634L };
	String[] code = { "F", "U", "B", "P", "S" };
	Integer[] from = { 60, 12, null, 32, 32 };
	Integer[] to = { 50, 9, null, 56, null };

	/**
	 * @return a event test list with an Collection interface
	 */
	List<Event> getEventList() {
		ArrayList<Event> eventList = new ArrayList<Event>(seqno.length);
		for (int i=0; i < seqno.length;i++) {
			StringBuffer buffer = new StringBuffer().append(seqno[i]).append(separator).append(code[i]);
			if (from[i] != null)
				buffer.append(separator).append(from[i]);
			if (to[i] != null)
				buffer.append(separator).append(to[i]);
			eventList.add(new Event(buffer.toString()));
		}		
		return eventList;
	}
	
	/**
	 * @return an event iterator to be reused in various tests
	 */
	Iterator<Event> getEventIterator() {
		return getEventList().iterator();
	}
}
