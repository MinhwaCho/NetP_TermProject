package client.room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Enumeration;
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
	
	private static final String C_UPDGAME = "305";		// Client -> Server ������ �˸�
	private static final String C_UPDSCORE = "307";		// Client -> Server ���� ���� �˸�
	
	private int[][] map;			// main map
	
	private boolean gameover = false;	// game ���
	private PlayerKeyboardListener pListener;
	
	private MapPanel mapPanel;
	
	// Server���� ����� ���� parent
	private WaitingView parent;
	private int roomKey;
	
	// item ����
	private Vector<Point> item = new Vector<Point>();
	private ImageIcon iIcon = new ImageIcon("res/item.png");
	private Image itemImg = iIcon.getImage();
	
	//bullet
	private static final int MAX_BULLET = 1;		// �ִ�� bullet�� �� �� �ִ� Ƚ��
	private int myBullet = 0;						// ���� ��� ������?
	private Point bullet = null;
	private BulletThread bulletThread = null;
	private Vector<Point> bullet_location = new Vector<Point>();
	private Image bulletImg = new ImageIcon("res/bullet.png").getImage();
	
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
		mapPanel = this;
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
		Iterator<String> it = playerXY.keySet().iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(userName.equals(myName))		// �� �׸���
				graphics2.drawImage(player, coordinate.x, coordinate.y, UNIT, UNIT, this);
			else
				graphics2.drawImage(player2, coordinate.x, coordinate.y, UNIT, UNIT, this);
		}
		
		// bullet �׸���
		for(Point p: bullet_location) {
			graphics2.drawImage(bulletImg, p.x, p.y, UNIT, UNIT, this);
		}
		
		g.drawImage(panelImage, 0, 0, this);

	}
	
	// player�� �����̴� keyBoard callBack
	class PlayerKeyboardListener extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch(keyCode) {
				// ����Ű�̸� �׳� ������
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					parent.SendObject(new ChatMsg(myName, C_UPDGAME, roomKey+"", keyCode));
					break;
	
				// Bullet�̸� ������ Ƚ�� �������� ������	
				case KeyEvent.VK_W:
				case KeyEvent.VK_S:
				case KeyEvent.VK_A:
				case KeyEvent.VK_D:
					if (myBullet < MAX_BULLET) {
						parent.SendObject(new ChatMsg(myName, C_UPDGAME, roomKey+"", keyCode));
						myBullet++;
					}
					break;
			}
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
			
		// bullet : MAX_BULLET �����϶��� �� �� �ִ�.
		case KeyEvent.VK_W:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(mapPanel, bullet, 0, -1);
			bulletThread.start();
			break;
		case KeyEvent.VK_S:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(mapPanel, bullet, 0, 1);
			bulletThread.start();
			break;
		case KeyEvent.VK_A:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(mapPanel, bullet, -1, 0);
			bulletThread.start();
			break;
		case KeyEvent.VK_D:
			bullet = new Point(coordinate.x, coordinate.y);
			bulletThread = new BulletThread(mapPanel, bullet, 1, 0);
			bulletThread.start();
			break;
		}
		
		// item�� ������ ������ ������Ų��.
		for(int i = 0; i < item.size(); i++) {
			Point p = item.get(i);
			if(p.x*UNIT == coordinate.x && p.y*UNIT == coordinate.y) {
				item.remove(i);
				if(userName.equals(myName))
					parent.SendObject(new ChatMsg(myName, C_UPDSCORE, roomKey + " " + "+", 10));
			}
		}
		
		// �ٽ� ����
		playerXY.put(userName, coordinate);
		
		repaint();
	}

	// bullet�� ��ġ ���� ���� + repaint
	public synchronized void bulletRepaint(int x, int y) {
		bullet_location.add(new Point(x,y));
		
		// �ٸ� player�� bullet�� ���� ���
		Iterator<String> it = playerXY.keySet().iterator();
		while(it.hasNext()) {
			String userName = it.next();
			Point coordinate = playerXY.get(userName);
			if(!userName.equals(myName)) {
				if(coordinate.x == x && coordinate.y == y) {
					parent.SendObject(new ChatMsg(userName, C_UPDSCORE, roomKey + " " + "-", 5));
				}
			}
		}
				
		repaint();
	}
	public synchronized void bulletRemove(int x, int y) {
		bullet_location.remove(new Point(x,y));
		myBullet--;		// �¾����Ƿ� �ٽ� �� �� �ִ� Ƚ�� ����.
		repaint();
	}
	

	class BulletThread extends Thread {
		private MapPanel mapPanel = null;
		private Point bullet = null;
		private int change_x;
		private int change_y;
		//private BulletRunnable bulletRunnable = null;
		
		public BulletThread(MapPanel mapPanel, Point bullet, int change_x, int change_y) {
			this.mapPanel = mapPanel;
			this.bullet = bullet;
			this.change_x=change_x;
			this.change_y=change_y;
		}
		
		
		public void run() {
			while(true) {
				if(change_y < 0) {
					if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.y > 0) {
						bullet.y -= UNIT;
						mapPanel.bulletRepaint(bullet.x, bullet.y);
					} 
					else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
						mapPanel.bulletRemove(bullet.x, bullet.y);
						return;
					}
				}
				else if(change_y > 0) {
					if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.y < 460) {
						bullet.y += UNIT;
						mapPanel.bulletRepaint(bullet.x, bullet.y);
					} 
					else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
						mapPanel.bulletRemove(bullet.x, bullet.y);
						return;
					}
				}
				else if(change_x < 0) {
					if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.x > 0) {
						bullet.x -= UNIT;
						mapPanel.bulletRepaint(bullet.x, bullet.y);
					} 
					else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
						mapPanel.bulletRemove(bullet.x, bullet.y);
						return;
					}
				}
				else if(change_x > 0) {
					if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) != 1 && bullet.x < 460) {
						bullet.x += UNIT;
						mapPanel.bulletRepaint(bullet.x, bullet.y);
					} 
					else if(mapPanel.getXY(bullet.x/UNIT + change_x, bullet.y/UNIT + change_y) == 1) {
						mapPanel.bulletRemove(bullet.x, bullet.y);
						return;
					}
				}
				 
				try { sleep(20); } catch (InterruptedException e) {e.printStackTrace();}
				mapPanel.bulletRemove(bullet.x, bullet.y);
			}
		 } // end of run
		
	}// end of BulletThread
}
