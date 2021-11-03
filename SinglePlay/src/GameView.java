import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;

public class GameView extends JFrame{
	
	private final int MAPNUM = 1;		// �ӽ÷� 1�� ���� : �渶�� �ٸ��� �غ���
	private final int ITEMS = 5;		// map���� ��Ÿ�� �������� ��
	
	private final int WIDTH = 420;		// component�� ���� ���̸� ���߱� ���� ����
	
	private Container contentPane;
	private MapPanel mapPanel;			// map�� ��Ÿ���� Panel
	private InfoPanel infoPanel;		// Timer�� Score�� ��Ÿ���� Panel
	
	// JFrame ���� : Swing Frame
	public GameView() {
		// �⺻ ����
		setTitle("SinglePlay Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));
		
		// Map ���̱�
		mapPanel = new MapPanel(MAPNUM, ITEMS, WIDTH);
		contentPane.add(mapPanel, BorderLayout.CENTER);
		
		// Timer Thread, Score ���̱�
		infoPanel = new InfoPanel(WIDTH);
		contentPane.add(infoPanel, BorderLayout.NORTH);
	

		// Frame ũ�� ����
		setSize(WIDTH, 500);
		setResizable(false);
		setVisible(true);
		
		// focus ����
		mapPanel.setFocusable(true);
		mapPanel.requestFocus();
	}

}
