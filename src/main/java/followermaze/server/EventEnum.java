package followermaze.server;

/**
 * represent the message type of an events. It can be constructed from the
 * character code
 */
public enum EventEnum {
	FOLLOW("F"), UNFOLLOW("U"), BROADCAST("B"), PRIVATEMSG("P"), STATUSUPDATE(
			"S");

	private final String name;

	private EventEnum(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString() {
		return name;
	}

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
