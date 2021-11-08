package client.room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import data.ChatMsg;
import data.GameMap;

public class MapPanel extends JPanel implements Serializable{

	private static final long serialVersionUID = 3L;
	
	private final int ROWS = 23;	// map�� ���α���
	private final int COLS = 23;	// map�� ���α���
	private final int UNIT = 20;	// map�� �� ĭ�� ���� (pixel)
	
	private static final String C_UPDGAME = "305";		// Client -> Server ������ 305
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game ���
	private PlayerKeyboardListener pListener;
	
	// Server���� ����� ���� parent
	private WaitingView parent;
	private int roomKey;
	
	// item ����
	private Vector<Point> item = new Vector<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image itemImg = iIcon.getImage();
	
	// player ����
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private ImageIcon pIcon2 = new ImageIcon("res/smile2.png");
	private Image player = pIcon.getImage();
	private Image player2 = pIcon2.getImage();
	
	private String myName;
	private HashMap<String, Point> playerXY;
	
	// ���� DoubleBuffering�� ���� �ڵ�
	private Image panelImage;
	private Graphics graphics;
	private Graphics graphics2;

	// num�� ���� �׿� �´� �̷� Map�� ����� ������
	public MapPanel(WaitingView parent, GameMap gameMap, int roomKey, String myName){
		this.parent = parent;
		this.roomKey = roomKey;
		this.myName = myName;
		
		map = gameMap.getMap();
		item = gameMap.getItem();
		playerXY = gameMap.getPlayerXY();

		setLayout(null);
		setPreferredSize(new Dimension(460, 460));
		
		graphics = this.getGraphics();
		
		pListener = new PlayerKeyboardListener();
		addKeyListener(pListener);
	}
	
	// row, col�� ��ǥ�� ���� ������ ������ �˻��ϴ� �Լ�
	public int getXY(int row, int col) {
		if (row < 0 || row > ROWS - 1 || col < 0 || col > COLS - 1)		// error ó��
			return 1;
		else
			return map[col][row];
	}
	
	// repaint()
	public void paint(Graphics g) {
		if (panelImage == null) {
			panelImage = createImage(this.getWidth(), this.getHeight());
			if(panelImage == null)
				System.out.println("PANELIMAGE CREATE ERROR!!");
			else
				graphics2 = panelImage.getGraphics();
		}
		update(g);
	}

	// ���� ���� ��Ȳ �׸���
	public void update(Graphics g) {
		// map �׸���
		for(int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (getXY(i, j) == 1)	graphics2.setColor(Color.DARK_GRAY);
				else 					graphics2.setColor(Color.LIGHT_GRAY);
				graphics2.fillRect(i*UNIT, j*UNIT, UNIT, UNIT);
			}
		}
		
		// item �׸���
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			graphics2.drawImage(itemImg, p.x*UNIT, p.y*UNIT, UNIT, UNIT, this);
		}

		// player �׸���
		Set<String> keys = playerXY.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(userName.equals(myName))		// �� �׸���
				graphics2.drawImage(player, coordinate.x, coordinate.y, UNIT, UNIT, this);
			else
				graphics2.drawImage(player2, coordinate.x, coordinate.y, UNIT, UNIT, this);
		}
		
		g.drawImage(panelImage, 0, 0, this);

	}
	
	// player�� �����̴� keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
			parent.SendObject(new ChatMsg(myName, C_UPDGAME, roomKey+"", e.getKeyCode()));
		}
	}
	
	// Server�κ��� ������ �޾� ���������� �̺�Ʈ�� ó���ϴ� �κ�
	public void doKeyEvent(String userName, int keyCode) {
		Point coordinate = playerXY.get(userName);
		System.out.println(userName + " :: " + coordinate.x + ", " + coordinate.y + ", " + keyCode);
		
		// ȭ��ǥ�� ���⿡ ���� �����̱�
		switch(keyCode) {
		case KeyEvent.VK_UP:
			if(getXY(coordinate.x/UNIT, coordinate.y/UNIT - 1) != 1 && coordinate.y > 0)
				coordinate.y -= UNIT;
			break;
		case KeyEvent.VK_DOWN:
			if(getXY(coordinate.x/UNIT, coordinate.y/UNIT + 1) != 1 && coordinate.y < 460)
				coordinate.y += UNIT;
			break;
		case KeyEvent.VK_LEFT:
			if(getXY(coordinate.x/UNIT - 1, coordinate.y/UNIT) != 1 && coordinate.x > 0)
				coordinate.x -= UNIT;
			break;
		case KeyEvent.VK_RIGHT:
			if(getXY(coordinate.x/UNIT + 1, coordinate.y/UNIT) != 1 && coordinate.x < 460)
				coordinate.x += UNIT;
			break;
		}
		
		// item�� ������ ������ ������Ų��.
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			if(p.x*UNIT == coordinate.x && p.y*UNIT == coordinate.y) {
				item.remove(i);
				//Score.addScore(10);
			}
		}
		
		// �ٽ� ����
		playerXY.remove(userName);
		playerXY.put(userName, coordinate);
		
		repaint();
	}

}