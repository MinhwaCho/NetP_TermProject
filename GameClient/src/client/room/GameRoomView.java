package client.room;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import data.ChatMsg;
import data.GameMap;
import data.GameRoom;

public class GameRoomView extends JFrame{
	
	// ���� userStatus ó���� ���� ����
	private static final String ONLINE = "ONLINE";
	private static final String SLEEP = "SLEEP";
	private static final String READYON = "READYON";
	private static final String READYOFF = "READYOFF";
	private static final String GAME = "GAME";
	
	private static final String C_UPDROOM = "302";
	private static final String C_STRGAME = "304";
	
	private WaitingView parent;
	private GameRoom room;

	private Container contentPane;
	private JLabel roomTitle;
	
	private JTextField txtInput;
	private JTextArea textArea;
	private JButton btnSend;
	private JButton startBtn;
	
	private String myName;
	
	private Vector<String> userList = new Vector<String>();
	private Vector<String> btnStatus = new Vector<String>();
	private Vector<JButton> readyBtn = new Vector<JButton>();
	
	private boolean isPressed = false;
	private boolean isStarted = false;
	
	private Color btnEnable = new Color(180, 210, 255);
	private Color btnDisable = new Color(200, 200, 200);
	private Color defaultColor = new Color(238, 238, 238);
	
	// ���� ���� ����
	private MapPanel mapPanel;
	private JLabel timerLabel;
	private Vector<JLabel> scoreInfo = new Vector<JLabel>();		// 1st, 2nd ...
	private Vector<UserScore> scores = new Vector<UserScore>();		// score ���� �� JLabel ����
	
	private ImageIcon rank1 = new ImageIcon("res/ranks/1.png");
	private ImageIcon rank2 = new ImageIcon("res/ranks/2.png");
	private ImageIcon rank3 = new ImageIcon("res/ranks/3.png");
	private ImageIcon rank4 = new ImageIcon("res/ranks/4.png");
	private ImageIcon[] ranks = {rank1, rank2, rank3, rank4};

	public GameRoomView(WaitingView parent, GameRoom room) {
		this.parent = parent;
		this.room = room;
		myName = parent.getMyName();
		
		// �⺻ ����
		setTitle("Network MAZE Game - " + "[#" + room.getKey() + "] "+room.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(null);
		
		roomTitle = new JLabel(" [#" + room.getKey() + "] "+room.getName());
		roomTitle.setOpaque(true);
		roomTitle.setBounds(10, 10, 200, 25);
		roomTitle.setBackground(btnDisable);
		roomTitle.setFont(new Font("���� ���", Font.BOLD, 15));
		contentPane.add(roomTitle);
		
		startBtn = new JButton("START");
		startBtn.setOpaque(true);
		startBtn.setBackground(btnDisable);
		startBtn.setFont(new Font("���� ���", Font.BOLD + Font.ITALIC, 12));
		startBtn.setBounds(10, 45, 200, 30);
		startBtn.setEnabled(false);
		contentPane.add(startBtn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 75, 200, 345);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setFont(new Font("����ü", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(10, 420, 130, 30);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("SEND");
		btnSend.setOpaque(true);
		btnSend.setBackground(btnEnable);
		btnSend.setFont(new Font("���� ���", Font.BOLD + Font.ITALIC, 12));
		btnSend.setBounds(140, 420, 70, 30);
		contentPane.add(btnSend);
		
		// ReadyBtn ����
		userList = room.getUserList();
		for (int i = 0; i < userList.size(); i++) {
			String userName = userList.get(i).toString();
			JButton ready = new JButton(userName);
			ready.setOpaque(true);
			ready.setBackground(btnDisable);
			ready.setFont(new Font("���� ���", Font.BOLD + Font.ITALIC, 20));
			ready.setBounds(220 + 165*i, 50, 160, 400);
			if(!userName.equals(myName))
				ready.setEnabled(false);
			ready.addActionListener(new ReadyActionListener(room.getKey()));
			contentPane.add(ready);
			readyBtn.add(ready);
		}
		
		// Event Listener ����
		TextSendAction action = new TextSendAction();
		btnSend.addActionListener(action);
		txtInput.addActionListener(action);
		startBtn.addActionListener(new StartActionListener(room.getKey()));

		// Frame ũ�� ����
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
				
		// focus ���� - Mouse Listener�� ���� �� �ְ� ��
		contentPane.setFocusable(true);
		contentPane.requestFocus();
	}
	
	// Server�κ��� Room�� ��������� ���� ���, revalidate(), repaint()�� ������Ʈ
	public void clear() {
		for (int i = 0; i < readyBtn.size(); i++) {
			contentPane.remove(readyBtn.get(i));
		}
		readyBtn.clear();
		userList.clear();
		btnStatus.clear();
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	// ���ο� User �߰�
	public void addUser(String name, String status) {
		JButton ready = new JButton(name);
		ready.setOpaque(true);
		if(status.equals(READYON))
			ready.setBackground(btnEnable);
		else
			ready.setBackground(btnDisable);
		ready.setFont(new Font("���� ���", Font.BOLD + Font.ITALIC, 20));
		ready.setBounds(220 + 165*readyBtn.size(), 50, 160, 400);
		
		if(!name.equals(myName))
			ready.setEnabled(false);
		else
			ready.addActionListener(new ReadyActionListener(room.getKey()));

		// startBtn Ȱ��ȭ üũ
		int i = 0;
		for (i = 0; i < btnStatus.size(); i++) {
			if (!btnStatus.get(i).equals(READYON))
				break;
		}
		if (i==btnStatus.size() && status.equals(READYON)) {
			startBtn.setEnabled(true);
			startBtn.setBackground(new Color(240, 200, 200));
		}
		else {
			startBtn.setEnabled(false);
			startBtn.setBackground(btnDisable);
		}
			
		contentPane.add(ready);
		userList.add(name);
		readyBtn.add(ready);
		btnStatus.add(status);
		
		contentPane.repaint();
		contentPane.revalidate();
	}
	
	// ȭ�鿡 ��� - Chatting
	public void AppendText(String msg) {
		textArea.append(msg + "\n");
		msg = msg.trim(); // �յ� blank�� \n�� �����Ѵ�.
		int len = textArea.getDocument().getLength();
		// ������ �̵�
		//textArea.setCaretPosition(len);
		//textArea.replaceSelection(msg + "\n");
	}
	
	// ���� ����
	// GameView�� ���Ӱ� �׸��� update��û
	public void startGame(GameMap gameMap) {
		isStarted = true;
		for (int i = 0; i < readyBtn.size(); i++) {
			contentPane.remove(readyBtn.get(i));
		}
		startBtn.setEnabled(false);
		startBtn.setBackground(btnDisable);
		
		System.out.println("CLIENT "+myName+" GAME STARTED");
		
		// Map �׸���
		mapPanel = new MapPanel(parent, gameMap, room.getKey(), myName);
		mapPanel.setBounds(220, 00, 460, 460);
		contentPane.add(mapPanel);
		
		// TimerLabel �׸���
		timerLabel = new JLabel(" ���� �ð� : "+99+" sec");		// �׳� ���Ƿ� �־�� 99. �̰� ��Ÿ���� ������� ��.
		timerLabel.setOpaque(true);
		timerLabel.setBackground(new Color(240, 200, 200));
		timerLabel.setBounds(690, 10, 185, 40);
		timerLabel.setFont(new Font("���� ���", Font.BOLD, 15));
		contentPane.add(timerLabel);
		
		// ScoreLabel �׸���
		for(int i = 0; i < userList.size(); i++) {
			String userName = userList.get(i);
			UserScore userScore = new UserScore(userName);
			
			// Rank ���̱�
			JLabel rank = new JLabel(ranks[i]);
			rank.setOpaque(true);
			rank.setBounds(690, 60 + 40*i, 70, 40);
			if(userName.equals(myName))	rank.setBackground(Color.WHITE);
			else						rank.setBackground(defaultColor);
			contentPane.add(rank);
			
			// userScore ���̱�
			JLabel info = new JLabel(" " + userName);
			info.setOpaque(true);
			info.setBounds(760, 60 + 40*i, 60, 40);
			info.setFont(new Font("���� ���", Font.BOLD, 15));
			if(userName.equals(myName))	info.setBackground(Color.WHITE);
			else						info.setBackground(defaultColor);
			userScore.setNameLabel(info);
			contentPane.add(info);
			
			userScore.setScore(0);
			JLabel score = new JLabel(" "+userScore.getScore());
			score.setOpaque(true);
			score.setBounds(820, 60 + 40*i, 55, 40);
			score.setFont(new Font("���� ���", Font.BOLD, 15));
			if(userName.equals(myName))	score.setBackground(Color.WHITE);
			else						score.setBackground(defaultColor);
			userScore.setScoreLabel(score);
			contentPane.add(score);
			
			scoreInfo.add(rank);
			scores.add(userScore);
		}

		contentPane.revalidate();
		contentPane.repaint(); 
		
		mapPanel.requestFocus();
	}
	
	// Server�κ��� ���� �ð��� �޾� update
	public void updateTimer(int timeout) {
		timerLabel.setText(" ���� �ð� : " + timeout + " �� ");
		// ���� �ð��� �˹��Ҽ��� ������ true RED�� ���������.
		if(timeout<=10)
			timerLabel.setBackground(new Color(240, 200-(10-timeout)*15, 200-(10-timeout)*15));
		
		contentPane.revalidate();
		contentPane.repaint(); 
		
		mapPanel.requestFocus();
	}
	
	// Server�κ��� ������ �޾� update
	public void updateScore(String userName, String cal, int score) {
		
		// �ش� user�� ���� update
		UserScore userScore = null;
		for (int i = 0; i < scores.size(); i++) {
			userScore = scores.get(i);
			if(userScore.getUserName().equals(userName))
				break;
		}
		
		int tempScore = userScore.getScore();
		
		switch(cal) {
		case "+":
			tempScore += score;
			break;
		case "-":
			tempScore -= score;
			break;
		}
		
		userScore.setScore(tempScore);
		
		// Label update	with sorting
		String tempName;
		JLabel tempNameLabel;
		JLabel tempScoreLabel;
		JLabel scoreInfoLabel;
		Collections.sort(scores, new UserScoreComparator());
		for (int i = 0; i < scores.size(); i++) {
			scoreInfoLabel = scoreInfo.get(i);
			userScore = scores.get(i);
			tempName = userScore.getUserName();
			
			tempNameLabel = userScore.getNameLabel();
			tempNameLabel.setBounds(760, 60 + 40*i, 60, 40);
			if(tempName.equals(myName)) {
				tempNameLabel.setBackground(Color.WHITE);
				scoreInfoLabel.setBackground(Color.WHITE);
			}
			else {
				tempNameLabel.setBackground(defaultColor);
				scoreInfoLabel.setBackground(defaultColor);
			}

			tempScoreLabel = userScore.getScoreLabel();
			tempScoreLabel.setBounds(820, 60 + 40*i, 55, 40);
			if (tempName.equals(userName))	tempScoreLabel.setText(" "+tempScore);	// ���� ���� ó��
			if(tempName.equals(myName))	tempScoreLabel.setBackground(Color.WHITE);
			else						tempScoreLabel.setBackground(defaultColor);
		}
		
		contentPane.revalidate();
		contentPane.repaint(); 
				
		mapPanel.requestFocus();
	}
	
	// Server�κ��� ���� �̺�Ʈ �����ϱ�
	public void doKeyEvent(String userName, int keyCode) {
		mapPanel.doKeyEvent(userName, keyCode);
	}
	
	// keyboard enter key ġ�� ������ ����
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button�� �����ų� �޽��� �Է��ϰ� Enter key ġ��
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				msg = txtInput.getText();
				parent.SendMessage(room.getKey(), msg);
				txtInput.setText(""); // �޼����� ������ ���� �޼��� ����â�� ����.
				if (mapPanel != null)
					mapPanel.requestFocus();
				else
					txtInput.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
			}
		}
	}
	
	// ReadyBtn�� ���� EventListener - ��ư�� ������ ready ���¸� �ٲٰ� server�� update ��û
	class ReadyActionListener implements ActionListener{
		private int key;
		public ReadyActionListener(int key) {
			this.key = key;
		}
		public void actionPerformed(ActionEvent e) {
			isPressed = !isPressed;
			parent.SendObject(new ChatMsg(myName, C_UPDROOM, key+" "+isPressed));
		}
	} // End of class ReadyActionListener
	
	// StartBtn�� ���� EventListener - ��ư�� ������ ready ���¸� üũ�ϰ� Game start
	class StartActionListener implements ActionListener{
		private int key;
		public StartActionListener(int key) {
			this.key = key;
		}
		public void actionPerformed(ActionEvent e) {
			parent.SendObject(new ChatMsg(myName, C_STRGAME, key+""));
		}
	} // End of class ReadyActionListener
}
