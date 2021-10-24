package client.wait;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RoomListPanel extends JPanel{

	private HashMap<Integer, String> roomInfo;		// ���� ID, �̸��� ������ HashMap
	private HashMap<Integer, RoomView> roomViews;	// �ش� �濡 �ش��ϴ� RoomView�� ������ HashMap
	
	public RoomListPanel(HashMap<Integer, String> roomInfo) {
		this.roomInfo = roomInfo;
		
		// room�� ���� JLabel, JButton ����
		roomViews = new HashMap<Integer, RoomView>();
		Set<Integer> roomInfoKeys = roomInfo.keySet();
		Iterator<Integer> roomInfoIt = roomInfoKeys.iterator();
		while(roomInfoIt.hasNext()) {
			int key = roomInfoIt.next();
			String name = roomInfo.get(key);
			RoomView roomView = new RoomView(key, name);
			roomViews.put(key, roomView);
			
			add(roomView.name);
			add(roomView.enter);
		}
		
		setPreferredSize(new Dimension(680, roomInfo.size() * 60));
	}
	
	public boolean addRoom(int key, String name) {
		try {
			// Room ��ü ������ �� hashMap (roomInfo, rooms)�� �ִ´�.
			Room room = new Room(key, name);
			RoomView roomView = new RoomView(key, name) ;
			roomInfo.put(key, name);
			roomViews.put(key, roomView);
			this.add(roomView.name);
			this.add(roomView.enter);
			
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean delRoom(Room room) {
		try {
			// Room ��ü�� hashmap�� �������� �����Ѵ�.
			int key = room.getKey();
			roomInfo.remove(key);
			
			RoomView roomView = roomViews.get(key);
			this.remove(roomView.name);
			this.remove(roomView.enter);
			
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public class RoomView{
		
		private int key;
		private JLabel name = new JLabel();
		private JButton enter = new JButton(" ENTER ");
		
		// ���� �� �ִ� ��ư�� �׷��� ���� ��ư ����
		private Color btnEnable = new Color(180, 210, 255);
		private Color btnDisable = new Color(200, 200, 200);
		
		public RoomView(int key, String roomName) {
			this.key = key;
			name.setText(" [" + key + "] " + roomName);
			name.setOpaque(true);
			name.setBackground(Color.WHITE);
			name.setPreferredSize(new Dimension(560, 50));
			name.setFont(new Font("���� ���", Font.BOLD, 20));
			
			// ���� �ο����� �߰��� ����
			
			enter.setOpaque(true);
			enter.setBackground(btnEnable);
			enter.setPreferredSize(new Dimension(100, 50));
			enter.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
			enter.addMouseListener(new EnterMouseListener(key));
			
			// ���� ���¸� �ľ��� Enable ���¸� �����ϴ� �ڵ�
			
		} // End of RoomView()
		
		public class EnterMouseListener extends MouseAdapter{
			private int key;
			public EnterMouseListener(int key) {
				this.key = key;
			}
			public void onMouseClicked(MouseEvent e) {
				// key�� �ش��ϴ� room�� ã��, �ش� room�� �����ϸ� AVAIL ���¸� �����Ѵ�.
			}
		} // End of class MouseListener
	} // End of class RoomView
} // End of class RoomListPanel
