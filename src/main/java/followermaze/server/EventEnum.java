package followermaze.server;

public enum EventEnum {
	FOLLOW("F"), UNFOLLOW("U"), BROADCAST("B"), PRIVATEMSG("P"), STATUSUPDATE("S");

	private final String	name;

	private EventEnum(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString() {
		return name;
	}
}
