package data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;

import data.RoomManager;

// ���ӹ濡 ���� ������ ��� Room
// ObjectStream ������ ���� ��� �Ӽ� �߰�
public class GameRoom implements Serializable{
	
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
	
	// new player enter
	public void enterUser(String client_socket) {
		if(status!=AVAIL){
			System.out.println("CANNOT ENTER");
			return;
		}
		
		userList.add(client_socket);
		
		if(userList.size() == MAXPLAYER)
			setStatus(FULL);
	}
	
	// player exit
	public void exitUser(String client_socket) {
		userList.remove(client_socket);
		
		if(userList.size() < 1)
			RoomManager.delRoom(this);
	}

}
