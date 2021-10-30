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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class WaitingView extends JFrame{
	
	// �������� �����ϱ� ���� �ڵ�. �׻� "�ڵ� �޼���"�� ���·� �����Ұ�.
	private static final String C_LOGIN = "100";		// ���ο� client ����
	private static final String C_ACKLIST = "101";		// C->S 101�� ���������� ����
	private static final String S_REQLIST = "110";		// S->C �����Ǿ� �ִ� room ���� ����
	private static final String S_SENLIST = "120";		// S->C �� room�� key, name ����
	private static final String C_MAKEROOM = "200";		// ���ο� �� ����
	private static final String C_ENTROOM = "201";		// �ش� �濡 ����
	private static final String S_UPDROOM = "210";		// room ��� update
	
	private static final int BUF_LEN = 128; //  Windows ó�� BUF_LEN �� ����
	private Socket socket; // �������
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
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
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
			
			SendMessage(C_LOGIN + " " + userName);		// �α��� ������ �������� ����
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
				if (name == null)	return;
				else{
					SendMessage(C_MAKEROOM+" "+key+" "+name+" "+userName);
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
					// String msg = dis.readUTF();
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0) {
						System.out.println("dis.read() < 0 error");
						try {
							dos.close();
							dis.close();
							socket.close();
							break;
						} catch (Exception ee) {
							break;
						}// catch�� ��
					}
					String	msg = new String(b, "euc-kr");
					msg = msg.trim(); // �յ� blank NULL, \n ��� ����
					System.out.println("Client get : " + msg);
					
					// words[0] : �ڵ�
					// words[1] : /to�� ���� cmd
					// words[2] : /to�� ��� ���� UserName, �� �� �����ϰ��� �ϴ� �޼���
					// ���� : �����ϰ��� �ϴ� �޼���
					String cmds[] = msg.split(" ");
					
					// S_REQLIST(110)
					// Server -> Client �޼����� ���� ������ Ȯ���ϰ� ���� Room�� ������ ������.
					if(cmds[0].equals(S_REQLIST)) {
						roomNum = Integer.parseInt(cmds[1]);
						System.out.println("Client get : "+roomNum);
						SendMessage(C_ACKLIST+" "+userName);
					}
					
					// S_SENLIST(120)
					// Server -> Client Room�� ���� ���� (Key, Name)�� ������.
					else if(cmds[0].equals(S_SENLIST)) {
						// ������ �޾� client�� HashMap room�� �����Ѵ�.
						int key = Integer.parseInt(cmds[1]);
						String name = cmds[2];
						rooms.put(key, name);
						roomListPanel.addRoom(key, name);	// ȭ�鿡 ���̵��� ó��
					}
					
					// S_UPDROOM(210)
					// Server -> Client Room ����� ���� �����ϰ� update ��û
					else if(cmds[0].equals(S_UPDROOM)) {
						int key = Integer.parseInt(cmds[1]);
						String name = cmds[2];
						makeDialog.setKey(key);				// �ش� key �ߺ� ���� ó��
						roomListPanel.addRoom(key, name);	// ȭ�鿡 ���̵��� ó��
					}
					
				} catch (IOException e) {
					System.out.println("dis.read() error");
					try {
						dos.close();
						dis.close();
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
			// dos.writeUTF(msg);
			System.out.println("client send : " + msg);
			byte[] bb;
			bb = MakePacket(msg);
			dos.write(bb, 0, bb.length);
		} catch (IOException e) {
			System.out.println("dos.write() error");
			try {
				dos.close();
				dis.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
}