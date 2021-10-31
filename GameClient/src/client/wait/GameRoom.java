package client.wait;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;


// ���ӹ濡 ���� ������ ��� Room
public class GameRoom {
	
	private final static int MAXPLAYER = 4;
	private final static String AVAIL = "AVAIL";
	private final static String FULL = "FULL";
	private final static String STARTED = "STARTED";
	
	private int key;		// ���� ���� ��ȣ
	private String name;	// ���� �̸�
	private String status;	// ���� ���� - Button�� Ȱ��ȭ ���θ� �����Ѵ�.
	private ArrayList userList;	// �濡 �����ϴ� User�� ��
	
	public GameRoom(int key, String name) {
		this.key = key;
		this.name = name;
		
		userList = new ArrayList();
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
	public ArrayList getUserList() {
		return userList;
	}


}
