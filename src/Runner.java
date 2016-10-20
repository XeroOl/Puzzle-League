import javax.swing.JFrame;

import controller.GameInput;
import display.Display;
import game.GameField;

public class Runner {
	public static void main(String[] args) {
		GameField p = new GameField.Builder().blockTypeCount(4).build();
		JFrame j = new JFrame();
		j.setVisible(true);
		j.setSize(500, 500);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Display d = new Display();
		j.add(d);
		GameInput g = new GameInput();
		g.down = false;
		g.left = false;
		g.right = false;
		g.up = false;
		g.swapping = false;
		g.raisingStack = false;
		p.update(g);
		d.update(p);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long time = System.currentTimeMillis();
		long wait = 1000 / 30;
		long now;
		while (true) {
			now = System.currentTimeMillis();
			if (now > time + wait) {
				time += wait;
				p.update(g);
				d.update(p);
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
