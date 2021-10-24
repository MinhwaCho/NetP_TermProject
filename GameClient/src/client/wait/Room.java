package client.wait;

// ���ӹ濡 ���� ������ ��� Room
public class Room {
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;
	private String name;
	
	private String status;
	
	public Room(int key, String name) {
		this.key = key;
		this.name = name;
		
		status = AVAIL;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getStatus() {
		return status;
	}

}
