import javax.swing.JLabel;

public class Timer extends Thread{
	
	private JLabel timerLabel;		// timer를 표시할 JLabel
	private int timeout = 60;		// default : 60sec
	private boolean timerStatus;	// timer의 작동 상태 확인
	
	public Timer(JLabel timerLabel) {
		this.timerLabel = timerLabel;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		timerStatus = false;
	}
	
	public boolean getStatus() {
		return timerStatus;
	}
	
	public void run() {
		timerStatus = true;		// timer 작동
		timerLabel.setText("남은 시간 : " + timeout + "초");
		
		while(true) {
			try {
				timeout--;
				Thread.sleep(1000);
				timerLabel.setText("남은 시간 : " + timeout + "초");
				
				// timeout 설정
				if(timeout <= 0) {
					timerStatus = false;		// timer 멈춤
					break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
