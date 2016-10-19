package display;

import game.Block;
import game.Player;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public class Display extends Canvas {
	BufferStrategy bs;

	public Display() {
		createBufferStrategy(2);
		bs = getBufferStrategy();
	}

	public void update(Player p) {
		if (bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
			if (bs == null)
				return;
		}
		Graphics g = bs.getDrawGraphics();
		for (int x = 0; x < Player.WIDTH; x++) {
			for (int y = 0; y < Player.HEIGHT; y++) {
				Block b = p.blockAt(x, y);
				if (b.getColor() != 0) {
					if (b.isTrash()) {

					} else {
						
					}
				}
			}
		}
	}
}
