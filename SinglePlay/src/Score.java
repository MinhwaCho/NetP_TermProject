import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class Score{
	
	private static JLabel scoreLabel;			// score ǥ���� JLabel
	private static int score = 0;
	
	private int r = 200;
	private int g = 200;
	private int b = 240;
	
	public Score(JLabel scoreLabel, int height) {
		this.scoreLabel = scoreLabel;
		scoreLabel.setOpaque(true);						// ������ �����ϱ� ����
		scoreLabel.setBackground(new Color(r, g, b));
		scoreLabel.setFont(new Font("���� ���", Font.BOLD, 20));
		scoreLabel.setPreferredSize(new Dimension(190, height));	// �� ũ�� ����
		update();
	}
	
	// ���� update
	public static void update() {
		scoreLabel.setText(" ���� ���� : " + score + " �� ");
	}

	public static void addScore(int scores) {
		score += scores;
		update();
	}
	
	public static void delScore(int scores) {
		// ���� ������ 0���̴�.
		if (score - scores <= 0)
			score = 0;
		else
			score -= scores;
		update();
	}
}
