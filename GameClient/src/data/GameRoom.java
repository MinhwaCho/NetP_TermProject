package data;

import java.io.Serializable;
import java.util.Vector;

// ���ӹ濡 ���� ������ ��� Room
// ObjectStream ������ ���� ��� �Ӽ� �߰�
public class GameRoom implements Serializable{
	
	private static final long serialVersionUID = 2L;
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;		// ���� ���� ��ȣ
	private String name;	// ���� �̸�
	private String status;	// ���� ���� - Button�� Ȱ��ȭ ���θ� �����Ѵ�.
	private Vector<String> userList;	// �濡 �����ϴ� User�� ��
	
	public GameRoom(int key, String name) {
		this.key = key;
		this.name = name;
		
		userList = new Vector<String>();
		status = AVAIL;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	// UserList ���
	public Vector getUserList() {
		return userList;
	}
	
	// new player enter
	public void enterUser(String userName) {
		if(status!=AVAIL){
			System.out.println("CANNOT ENTER");
			return;
		}
		
		userList.add(userName);
		
		if(userList.size() == MAXPLAYER)
			setStatus(FULL);
	}
	
	// player exit
		public int exitUser(String userName) {
			userList.remove(userName);
			return userList.size();
		}

}
