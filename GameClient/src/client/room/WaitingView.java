package client.room;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import data.ChatMsg;
import data.GameRoom;
import data.RoomMsg;

public class WaitingView extends JFrame{
	
	// �������� �����ϱ� ���� �ڵ�. �׻� "�ڵ� �޼���"�� ���·� �����Ұ�.
	// ���� Protocol msg
	private static final String C_LOGIN = "100";		// ���ο� client ����
	private static final String C_ACKLIST = "101";		// C->S 101�� ���������� ����
	private static final String C_MAKEROOM = "200";		// ���ο� �� ����
	private static final String C_ENTROOM = "201";		// �ش� �濡 ���� 
	
	private static final String S_REQLIST = "110";		// S->C �����Ǿ� �ִ� room ���� ����
	private static final String S_SENLIST = "120";		// S->C �� room�� key, name ����
	private static final String S_UPDROOM = "210";		// room ��� update
	private static final String S_ENTROOM = "220";		// S->C room ���� �㰡
	
	private static final int BUF_LEN = 128; //  Windows ó�� BUF_LEN �� ����
	private Socket socket; // �������
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	private String userName;
	
	private Container contentPane;
	private JScrollPane scrollPane;
	private RoomListPanel roomListPanel;
	private MakeRoomDialog makeDialog;

	private HashMap<Integer, String> rooms;
	private int roomNum = 0;
	
	private JButton makeRoom;
	private String btnText = "<HTML><body><center>MAKE<br>NEW ROOM</center></body></HTML>";
	
	private ImageIcon img = new ImageIcon("res/howToPlay.jpg");
	private JLabel imgLabel = new JLabel(img);
	
	// JFrame ���� : Swing Frame
	public WaitingView(String username, String ip_addr, String port_no) {
		this.userName = username;
		
		// client codes
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			SendObject(new ChatMsg(userName, C_LOGIN, ""));		// �α��� ������ �������� ����
			ListenNetwork net = new ListenNetwork();
			net.start();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("connect error");
		}

		// �⺻ ����
		setTitle("Network MAZE Game - Waiting Room");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);

		// �� ����Ʈ�� ǥ���ϴ� RoomListPanel ���̱�
		rooms = new HashMap<Integer, String>();
		roomListPanel = new RoomListPanel(rooms);
		scrollPane
			= new JScrollPane(roomListPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setLocation(0, 3);
		scrollPane.setSize(new Dimension(700, 500));
		contentPane.add(scrollPane);
		
		// Dialog �����
		makeDialog = new MakeRoomDialog(this, "Make New Room");
				
		// makeRoombtn ���̱�
		makeRoom = new JButton(btnText);
		makeRoom.setOpaque(true);
		makeRoom.setBackground(new Color(220, 250, 200));
		makeRoom.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 15));
		makeRoom.setLocation(705, 8);
		makeRoom.setSize(new Dimension(170, 70));
		makeRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int key = makeDialog.getKey();
				makeDialog.setVisible(true);

				// make btn�� ���� return �� ���
				String name = makeDialog.getInput();
				if (name == null) {
					return;
				}
				else{
					SendObject(new ChatMsg(userName, C_MAKEROOM, key+" "+name));
				}
			}
		});
		contentPane.add(makeRoom);
		
		// ��� ���� ���̱�
		imgLabel.setLocation(705, 85);
		imgLabel.setSize(new Dimension(170, 360));
		contentPane.add(imgLabel, BorderLayout.EAST);

		// Frame ũ�� ����
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
		
		// focus ���� - Mouse Listener�� ���� �� �ְ� ��
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
	
	// Room���� Server���� ������ ���� WaitingView�� �Ѱ���� �Ѵ�.
	public WaitingView getWaitingView() {
		return this;
	}

	// Server Message�� �����ؼ� ȭ�鿡 ǥ��
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					RoomMsg rm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						
						String ccode = cm.getCode();
						String cdata = cm.getData();
						System.out.println("CLIENT GET DATA : "+ccode+" "+cdata);
						
						// ���� Protocol ó�� - ChatMsg ����

						// S_REQLIST(110)
						// Server -> Client �޼����� ���� ������ Ȯ���ϰ� ���� Room�� ������ ������.
						if(ccode.equals(S_REQLIST)) {
							roomNum = Integer.parseInt(cm.getData());
							System.out.println("CLIENT GET ROOMNUM : "+roomNum);
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
							
						// S_SENLIST(120)
						// Server -> Client Room�� ���� ���� (Key, Name)�� �޴´�.
						else if(ccode.equals(S_SENLIST)) {
							// ������ �޾� client�� HashMap room�� �����Ѵ�.
							String val[] = cm.getData().split(" ");
							int key = Integer.parseInt(val[0]);
							String name = val[1];
							String status = val[2];
							rooms.put(key, name);
							roomListPanel.addRoom(key, name, status);	// ȭ�鿡 ���̵��� ó��
						}
						
						// S_UPDROOM(210)
						// Server -> Client Room ����� ���� �����ϰ� update ��û	
						// Client�� ���� roomView�� reset�ϰ� �ٽ� �޾ƿ´�.
						else if(ccode.equals(S_UPDROOM)) {
							roomListPanel.clear();
							rooms.clear();
							roomNum = Integer.parseInt(cm.getData());
							System.out.println("CLIENT GET ROOMNUM : "+roomNum);
							SendObject(new ChatMsg(userName, C_ACKLIST, ""));
						}
					}
					else if (obcm instanceof RoomMsg) {
						rm = (RoomMsg) obcm;
						
						String rcode = rm.getCode();
						System.out.println("CLIENT GET DATA : "+rcode);
						
						// ���� Protocol ó�� - RoomMsg ����
						
						// S_ENTROOM(220)
						// Server -> Client �ش� client�� room�� �����ϵ��� �㰡.
						// GameView�� ����� Room�� ������ �޾� ��� ���´�.	
						if(rcode.equals(S_ENTROOM)) {
							GameRoom room = rm.getRoom();
							System.out.println("CLIENT GET ROOM : "+room.getUserList());
							new Thread(){
								public void run() {
									try {
										// GameRoomView ����
										GameRoomView gameRoomView = new GameRoomView(getWaitingView(), room);
										gameRoomView.setVisible(true);
										// ���� WaitingView ���� : �ٸ� �濡 �������� ���ϵ��� ����
										getWaitingView().setVisible(false);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}.run();
						}
					}
					else
						continue;

					
				} catch (IOException e) {
					System.out.println("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			}
		}
	} // End of ListenNetwork
	
	// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	} // End of MakePacket(msg)

	// Server���� network���� ����
	public void SendMessage(String msg) {
		try {
			oos.writeObject(new ChatMsg(userName, "200", msg));
		} catch (IOException e) {
			System.out.println("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public void SendObject(Object ob) { // ������ �޼����� ������ �޼ҵ�
		try {
			ChatMsg cm = (ChatMsg) ob;
			oos.writeObject(ob);
		} catch (IOException e) {
			System.out.println("�޼��� �۽� ����!!\n");
		}
	}

	// RoomList�� ǥ���ϴ� Panel
	public class RoomListPanel extends JPanel{

		private HashMap<Integer, String> roomInfo;		// ���� ID, �̸��� ������ HashMap
		private HashMap<Integer, RoomView> roomViews;	// �ش� �濡 �ش��ϴ� RoomView�� ������ HashMap
		
		// GameRoom������ STATUS ǥ��
		private final static String AVAIL = "AVAIL";
		private final static String FULL = "FULL";
		private final static String STARTED = "STARTED";
		
		// ���� �� �ִ� ��ư�� �׷��� ���� ��ư ����
		private Color btnEnable = new Color(180, 210, 255);
		private Color btnDisable = new Color(200, 200, 200);
			
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
		
		// RoomView�� �ٽ� �׸��� ���� ��� RoomView�� �����Ѵ�.
		public void clear() {
			Set<Integer> roomInfoKeys = roomInfo.keySet();
			Iterator<Integer> roomInfoIt = roomInfoKeys.iterator();
			while(roomInfoIt.hasNext()) {
				int key = roomInfoIt.next();
				System.out.println("CLIENT REMOVE : "+key);
				RoomView roomView = roomViews.get(key);
				this.remove(roomView.name);
				this.remove(roomView.enter);
			}
			
			roomInfo.clear();
			roomViews.clear();
			setPreferredSize(new Dimension(680, roomInfo.size() * 60));
			revalidate();
		}
		
		public boolean addRoom(int key, String name, String status) {
			try {
				// Room�� ������ hashMap (roomInfo, rooms)�� �ִ´�.
				RoomView roomView = new RoomView(key, name);
				roomInfo.put(key, name);
				roomViews.put(key, roomView);
				this.add(roomView.name);
				this.add(roomView.enter);
				
				if(!status.equals(AVAIL)) {
					roomView.enter.setBackground(btnDisable);
					roomView.enter.setEnabled(false);
				}
				
				setPreferredSize(new Dimension(680, roomInfo.size() * 60));
				revalidate();
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean delRoom(GameRoom gameRoom) {
			try {
				// Room ��ü�� hashmap�� �������� �����Ѵ�.
				int key = gameRoom.getKey();
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
				enter.addActionListener(new EnterActionListener(key));
			} // End of RoomView()
		} // End of class RoomView
		
		// RoomListPanel�� ���� btnListener - ����� ���� WaitingView�� �߰�
		public class EnterActionListener implements ActionListener{
			private int key;
			public EnterActionListener(int key) {
				this.key = key;
			}
			public void actionPerformed(ActionEvent e) {
				SendObject(new ChatMsg(userName, C_ENTROOM, key+""));
			}
		} // End of class MouseListener
	} // End of class RoomListPanel
}