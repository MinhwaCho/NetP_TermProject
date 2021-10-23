import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class GameView extends JFrame{
	
	private final int MAPNUM = 1;		// 임시로 1로 설정 : 방마다 다르게 해보기
	
	private Container container;
	private MapPanel mapPanel;
	
	// JFrame 생성 : Swing Frame
	public GameView() {
		// 기본 설정
		setTitle("SinglePlay Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//container = getContentPane();
		//container.setLayout(null);
		
		// Map 붙이기 : (0, 0) ~ (400, 400)
		mapPanel = new MapPanel(MAPNUM);
		setContentPane(mapPanel);
		mapPanel.setLocation(0, 0);
		mapPanel.setSize(mapPanel.getRow()*mapPanel.getUnit(), mapPanel.getCol()*mapPanel.getUnit());
		//container.add(mapPanel);

		// Frame 크기 설정
		setSize(900, 500);
		setResizable(false);
		setVisible(true);
		
		// focus 지정
		//container.setFocusable(true);
		//container.requestFocus();
		mapPanel.setFocusable(true);
		mapPanel.requestFocus();
	}

}
