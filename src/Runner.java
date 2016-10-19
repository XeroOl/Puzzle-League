import javax.swing.JFrame;

import display.Display;
import display.GameInput;
import game.Player;

public class Runner {
	public static void main(String[] args) {
		Player p = new Player.Builder().blockTypeCount(4).build();
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
		while (true) {
			g.swapping = Math.random() * 100 < 1;

			p.update(g);
			d.update(p);
		}
	}
}
