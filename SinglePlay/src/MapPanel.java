
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class MapPanel extends JPanel{
	
	private final int MAXMAP = 2;	// map의 총 개수
	private final int ROWS = 20;	// map의 가로길이
	private final int COLS = 20;	// map의 세로길이
	private final int UNIT = 20;	// map의 한 칸의 길이 (pixel)
	
	private int[][] map;			// main map
	
	private ImageIcon pIcon = new ImageIcon("res/smile.png");
	private Image player = pIcon.getImage();
	private int playerX = UNIT;
	private int playerY = 0;

	// num에 따라 그에 맞는 미로 Map을 만드는 생성자
	public MapPanel(int num){
		initMap(num);
		addKeyListener(new PlayerKeyboardListener());
		
	}
	
	private void initMap(int num) {
		int [][] map1 = 
			 {
				{1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,1,0,0,0,1,0,0,1,1,1,1,0,0,0,0,1,1,1},
				{1,0,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1,1},
				{1,0,1,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,1},
				{1,0,1,1,1,1,0,0,1,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,0,1,1,1,1,1,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,0,1,0,0,0,0,1},
				{1,0,1,0,0,1,0,0,1,0,1,0,1,1,1,0,0,1,0,1},
				{1,0,1,1,0,1,1,0,0,0,1,0,0,0,0,0,0,1,0,1},
				{1,0,0,0,0,0,1,0,1,0,1,1,1,1,1,1,1,1,0,1},
				{1,1,0,0,1,1,1,0,1,1,0,0,1,0,0,0,0,1,0,1},
				{1,1,1,0,0,0,1,0,0,0,0,1,1,1,0,1,0,1,0,5},
				{1,0,1,1,0,0,1,1,1,1,0,1,0,0,0,1,0,1,1,1},
				{1,0,0,1,0,0,1,0,0,1,0,1,0,0,0,1,0,0,0,1},
				{1,0,1,1,0,0,1,0,0,1,0,1,1,1,1,1,1,0,1,1},
				{1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1},
				{1,1,0,1,1,1,1,1,1,1,0,0,0,1,1,1,1,0,1,1},
				{1,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1},
				{1,1,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
			};
		
		int [][] map2 = 
			{
				{1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,0,1,0,0,0,0,0,0,1,0,0,0,1,1,0,0,0,1},
				{1,0,1,1,1,1,1,0,1,1,1,0,1,0,0,0,0,1,0,1},
				{1,0,0,0,0,0,1,0,0,0,0,0,1,1,1,1,1,1,0,1},
				{1,0,1,1,0,0,1,0,1,1,1,1,1,0,0,0,0,0,0,1},
				{1,0,1,0,0,1,1,0,0,0,0,0,1,1,0,0,1,1,1,1},
				{1,0,1,0,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1},
				{1,0,1,1,1,1,1,1,0,1,1,1,1,0,1,0,0,0,0,1},
				{1,0,1,0,0,1,0,1,0,1,0,0,0,0,1,1,0,1,1,1},
				{1,0,0,0,0,0,0,1,0,1,0,0,1,1,1,1,0,0,1,1},
				{1,0,1,1,1,1,0,1,0,1,0,0,0,0,0,0,1,0,0,1},
				{1,0,1,0,0,1,0,1,0,1,1,1,1,1,0,1,1,1,0,5},
				{1,0,1,0,1,1,0,1,0,0,0,1,0,1,0,0,0,1,0,1},
				{1,0,1,0,0,0,0,1,0,1,0,1,0,0,1,1,0,1,1,1},
				{1,0,1,1,1,0,0,1,0,1,0,0,0,0,0,1,0,0,1,1},
				{1,0,0,0,1,1,1,1,0,1,1,1,0,1,1,1,1,0,1,1},
				{1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,1},
				{1,0,1,1,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,1},
				{1,0,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
			};
		
		if (num % 2 == 1)	map = map1;
		else				map = map2;
	}
	
	// Getter
	public int getRow() {
		return ROWS;
	}
	public int getCol() {
		return COLS;
	}
	public int getUnit() {
		return UNIT;
	}

	public int getXY(int row, int col) {
		return map[col][row];
	}
	
	// 게임 진행 상황 그리기
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// map 그리기
		for(int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (getXY(i, j) == 1)	g.setColor(Color.DARK_GRAY);
				else 					g.setColor(Color.LIGHT_GRAY);
				g.fillRect(i*UNIT, j*UNIT, UNIT, UNIT);
			}
		}
		
		// player 그리기
		g.drawImage(player, playerX, playerY, UNIT, UNIT, this);
	}
	
	class PlayerKeyboardListener extends KeyAdapter{
		
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			
			System.out.println(playerX + ", " + playerY + ", " + keyCode);
			
			switch(keyCode) {
			case KeyEvent.VK_UP:
				if(getXY(playerX/UNIT, playerY/UNIT - 1) != 1 && playerY > 0)
					playerY -= UNIT;
				break;
			case KeyEvent.VK_DOWN:
				if(getXY(playerX/UNIT, playerY/UNIT + 1) != 1 && playerY < 400)
					playerY += UNIT;
				break;
			case KeyEvent.VK_LEFT:
				if(getXY(playerX/UNIT - 1, playerY/UNIT) != 1 && playerX > 0)
					playerX -= UNIT;
				break;
			case KeyEvent.VK_RIGHT:
				if(getXY(playerX/UNIT + 1, playerY/UNIT) != 1 && playerX < 400)
					playerX += UNIT;
				break;
			}
			
			repaint();
		}
	}
}