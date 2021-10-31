package client.wait;

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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import data.ChatMsg;

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
	
	// Server Message�� �����ؼ� ȭ�鿡 ǥ��
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
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
					} else
						continue;
					
					// ���� Protocol ó��
					String code = cm.getCode();
					String datas = cm.getData();
					System.out.println("CLIENT GET DATA : "+code+" "+datas);

					// S_REQLIST(110)
					// Server -> Client �޼����� ���� ������ Ȯ���ϰ� ���� Room�� ������ ������.
					if(code.equals(S_REQLIST)) {
						roomNum = Integer.parseInt(cm.getData());
						System.out.println("CLIENT GET ROOMNUM : "+roomNum);
						SendObject(new ChatMsg(userName, C_ACKLIST, ""));
					}
						
					// S_SENLIST(120)
					// Server -> Client Room�� ���� ���� (Key, Name)�� �޴´�.
					else if(code.equals(S_SENLIST)) {
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
					else if(code.equals(S_UPDROOM)) {
						roomListPanel.clear();
						rooms.clear();
						roomNum = Integer.parseInt(cm.getData());
						System.out.println("CLIENT GET ROOMNUM : "+roomNum);
						SendObject(new ChatMsg(userName, C_ACKLIST, ""));
					}
						
					// S_ENTROOM(220)
					// Server -> Client �ش� client�� room�� �����ϵ��� �㰡.
					// GameView�� ����� Room�� ������ �޾� ��� ���´�.	
					else if(code.equals(S_ENTROOM)) {
						String val[] = cm.getData().split(" ");
						int key = Integer.parseInt(val[0]);
						String name = val[1];
					}

					
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
	}
	
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
	}

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
			oos.writeObject(ob);
		} catch (IOException e) {
			System.out.println("�޼��� �۽� ����!!\n");
		}
	}
}