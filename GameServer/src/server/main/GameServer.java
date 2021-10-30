package server.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import server.room.Room;

public class GameServer extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

	// Room�� ���� ���� ����
	private HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();


	// Create the frame.
	public GameServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 386);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 244);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(12, 264, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(111, 264, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 300, 300, 35);
		contentPane.add(btnServerStart);
	}

	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					AppendText("����� ����. ���� ������ �� " + UserVec.size());
					new_user.start(); // ���� ��ü�� ������ ����
				} catch (IOException e) {
					AppendText("!!!! accept ���� �߻�... !!!!");
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �� �����Ǵ� Thread
	// Read One ���� ��� -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket client_socket;
		private Vector user_vc;
		private String UserName = "";
		private String UserStatus = "O";
		
		// ���� Protocol msg
		private static final String C_LOGIN = "100";		// ���ο� client ����
		private static final String C_ACKLIST = "101";		// C->S 101�� ���������� ����
		private static final String S_REQLIST = "110";		// S->C �����Ǿ� �ִ� room ���� ����
		private static final String S_SENLIST = "120";		// S->C �� room�� key, name ����
		private static final String C_MAKEROOM = "200";		// ���ο� �� ����
		private static final String C_ENTROOM = "201";		// �ش� �濡 ���� 
		private static final String S_UPDROOM = "210";		// room ��� update
		
		public String getUserName() {
			return UserName;
		}
		
		public String getUserStatus() {
			return UserStatus;
		}
		public void setUserStatus(String status) {
			this.UserStatus = status;
		}

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// �Ű������� �Ѿ�� �ڷ� ����
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
				is = client_socket.getInputStream();
				dis = new DataInputStream(is);
				os = client_socket.getOutputStream();
				dos = new DataOutputStream(os);
				// line1 = dis.readUTF();
				// /login user1 ==> msg[0] msg[1]
				byte[] b = new byte[BUF_LEN];
				dis.read(b);
				String line1 = new String(b);
				String[] msg = line1.split(" ");
				UserName = msg[1].trim();
				AppendText("���ο� ������ " + UserName + " ����.");
				WriteOne(S_REQLIST+" " + rooms.size());
			} catch (Exception e) {
				AppendText("userService error");
				e.printStackTrace();
			}
		}

		// ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				// sleep ������ �����ڴ� �����Ѵ�.
				if(!(user.getUserStatus().equals("S")))
					user.WriteOne(str);
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
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread�� ����ϴ� Client ���� 1:1 ����
		public void WriteOne(String msg) {
			try {
				// dos.writeUTF(msg);
				byte[] bb;
				bb = MakePacket(msg);
				dos.write(bb, 0, bb.length);
			} catch (IOException e) {
				AppendText("dos.write() error");
				try {
					dos.close();
					dis.close();
					client_socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				UserVec.removeElement(this); // �������� ���� ��ü�� ���Ϳ��� �����
				AppendText("����� ����. ���� ������ �� " + UserVec.size());
			}
		}

		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					// String msg = dis.readUTF();
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0) {
						AppendText("dis.read() < 0 error");
						try {
							dos.close();
							dis.close();
							client_socket.close();
							UserVec.removeElement(this); // �������� ���� ��ü�� ���Ϳ��� �����
							AppendText("����� ����. ���� ������ �� " + UserVec.size());
							break;
						} catch (Exception ee) {
							break;
						} // catch�� ��
					}
					String msg = new String(b, "euc-kr");
					msg = msg.trim(); // �յ� blank NULL, \n ��� ����
					AppendText(msg); // server ȭ�鿡 ���
					
					// words[0] : �ڵ�
					// ���� : �����ϰ��� �ϴ� �޼���
					String cmds[] = msg.split(" ");
					
					// ���� Protocol ó��
					
					// C_ACKLIST(101)
					// Client -> Server S_REQLIST�� ���������� ������ �˸�
					// Waiting Room�� ǥ���� Room���� �迭�� ������.
					if(cmds[0].equals(C_ACKLIST)) {
						Set<Integer> roomKeys = rooms.keySet();
						Iterator<Integer> roomKeysIt = roomKeys.iterator();
						for(int i = 0; i < rooms.size(); i++) {
							int key = roomKeysIt.next();
							String name = rooms.get(key).getName();
							WriteOne(S_SENLIST+" "+key+" "+name);
						}
					}
					
					// C_MAKEROOM(200)
					// client -> Server ���ο� ���� ������ּ���
					// Server�� hashMap�� key�� name�� ���� Room�� �����
					// �ش� Room ��ü return
					else if(cmds[0].equals(C_MAKEROOM)) {
						int key = Integer.parseInt(cmds[1]);
						String name = cmds[2];
						Room room = new Room(key, name);
						rooms.put(key, room);
						WriteAll(S_UPDROOM+" "+key+" "+name);
					}
					
					// exit ó��
					else if(cmds[1].equals("/exit")) {
						dos.close();
						dis.close();
						client_socket.close();
						UserVec.removeElement(this);
						WriteAll("[" + UserName + "] ���� �����ϼ̽��ϴ�.\n");
						AppendText("����� ����. ���� ������ �� " + UserVec.size());
						break;
					}
						
					else {
						WriteAll(msg + "\n"); // Write All
					}
					
				} catch (IOException e) {
					AppendText("dis.read() error");
					try {
						dos.close();
						dis.close();
						client_socket.close();
						UserVec.removeElement(this); // �������� ���� ��ü�� ���Ϳ��� �����
						AppendText("����� ����. ���� ������ �� " + UserVec.size());
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			} // while
		} // run
	}
}
