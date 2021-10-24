package client.game;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class InfoPanel extends JPanel{
	
	private final int HEIGHT = 30;
	
	private JLabel timerLabel = new JLabel();
	private JLabel scoreLabel = new JLabel();
	
	private Timer timer = new Timer(timerLabel, HEIGHT);
	private Score score = new Score(scoreLabel, HEIGHT);

	// num�� ���� �׿� �´� �̷� Map�� ����� ������
	public InfoPanel(int width){
		setLayout(new FlowLayout());
		System.out.println(timerLabel.getHeight()+"");
		setPreferredSize(new Dimension((int)width, HEIGHT + 5));
		
		// Label ���̱�
		add(timerLabel);
		add(scoreLabel);
		
		// �۵� ����
		timer.start();
	}
}
