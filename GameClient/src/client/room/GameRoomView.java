package client.room;

import java.awt.Container;

import javax.swing.JFrame;

import data.GameRoom;

public class GameRoomView extends JFrame{
	
	private WaitingView parent;
	private GameRoom room;
	
	private Container contentPane;
	
	public GameRoomView(WaitingView parent, GameRoom room) {
		this.parent = parent;
		this.room = room;
		
		// �⺻ ����
		setTitle("Network MAZE Game - " + "[#" + room.getKey() + "] "+room.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);

		// Frame ũ�� ����
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
				
		// focus ���� - Mouse Listener�� ���� �� �ְ� ��
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
}
