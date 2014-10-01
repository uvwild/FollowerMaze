package followermaze.server;

/**
 * represent the message type of an event according to spec
 * */
public enum EventEnum {
	FOLLOW("F"), UNFOLLOW("U"), BROADCAST("B"), PRIVATEMSG("P"), STATUSUPDATE("S");

	private final String name;

	private EventEnum(String s) {
		name = s;
	}

	/**
	 * @param otherName
	 * @return equality
	 */
	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString() {
		return name;
	}

	/**
	 * the EventEnum can be constructed directly from the character code. <br>
	 * This way we can use this CTOR method to parse the event type of the event message.
	 * */
	public static EventEnum fromString(String name) {
		if (name != null) {
			for (EventEnum b : EventEnum.values()) {
				if (name.equalsIgnoreCase(b.name)) {
					return b;
				}
			}
		}
		return null;
	}

}
