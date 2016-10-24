import game.GameField;

import javax.swing.JFrame;

import controller.GameInput;
import controller.MouseController;
import display.Display;

public class Runner {
	public static void main(String[] args) {
		GameField gf = new GameField.Builder().blockTypeCount(5).fallSpeed(1).build();
		JFrame j = new JFrame();
		j.setSize(400, 500);
		j.setVisible(true);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Display d = new Display();
		j.getContentPane().add(d);
		MouseController m = new MouseController(d, gf);
		GameInput g = m.getInput();
		j.validate();
		long time = System.currentTimeMillis();
		long wait = 1000 / 3;
		long now;
		boolean lag = false;
		while (true) {
			now = System.currentTimeMillis();
			if (now > time + wait) {
				if (lag) {
					System.out.println("Catching up");
				}
				lag = true;
				time += wait;
				g = m.getInput();
				gf.update(g);
				d.update(gf);
			} else {
				try {
					lag = false;
					Thread.sleep(time + wait - now);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
