package followermaze.server;

import java.util.Comparator;

/**
 * @author uv.wildner the event is basic java bean and can be created from its
 *         serialized text form with via the CTOR
 */
public class Event implements Comparator<Event>, Comparable<Event> {
	private Long sequenceNo;
	private EventEnum type;
	private Integer fromUserId;
	private Integer toUserId;

	public Event(String event) {
		String[] list = event.split("\\|");
		if (list.length > 0)
			sequenceNo = Long.decode(list[0]);
		if (list.length > 1)
			type = EventEnum.fromString(list[1]);
		if (list.length > 2)
			fromUserId = Integer.decode(list[2]);
		if (list.length > 3)
			toUserId = Integer.decode(list[3]);
	}

	public Long getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(Long sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public EventEnum getType() {
		return type;
	}

	public void setType(EventEnum type) {
		this.type = type;
	}

	public Integer getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Integer getToUserId() {
		return toUserId;
	}

	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}

	public int compare(Event o1, Event o2) {
		return o1.compareTo(o2);
	}

	public int compareTo(Event o) {
		return sequenceNo.compareTo(o.sequenceNo);
	}

}
