import game.GameField;

import javax.swing.JFrame;

import controller.GameInput;
import controller.MouseController;
import display.Display;

public class Runner {
	public static void main(String[] args) {
		GameField gf = new GameField.Builder().blockTypeCount(4).build();
		JFrame j = new JFrame();
		j.setVisible(true);
		j.setSize(500, 500);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Display d = new Display();
		j.add(d);
		MouseController m = new MouseController(d, gf);
		GameInput g = m.getInput();
		long time = System.currentTimeMillis();
		long wait = 1000 / 30;
		long now;
		while (true) {
			now = System.currentTimeMillis();
			if (now > time + wait) {
				time += wait;
				g = m.getInput();
				gf.update(g);
				d.update(gf);
			} else {
				try {
					Thread.sleep(time + wait - now);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}
