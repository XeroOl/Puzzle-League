package display;

import game.Block;
import game.Player;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Display extends Canvas {
	BufferStrategy bs;
	int theme = 1;
	String themepath = "assets/theme" + theme + "/";
	static int tilesize = 16;
	static Map<String, Image> images = new HashMap<String, Image>();

	public Display() {
	}

	public void update(Player p) {
		if (bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
			if (bs == null)
				return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(get(themepath + "background1.png"), 0, 0, null);
		for (int x = 0; x < Player.WIDTH; x++) {
			for (int y = 0; y < Player.HEIGHT; y++) {
				Block b = p.blockAt(x, y);
				if (b.getColor() != 0) {
					if (b.isTrash()) {
						//ignore it for now
					} else if (b.inMatchAnimation()) {
						g.drawImage(get(themepath + "match" + b.getChainNum() + ".png"), x * 16, y * 16, null);
					} else {
						g.drawImage(get(themepath + "block" + b.getColor() + ".png"), x * 16 + b.getSwapAnim() * 2,
								y * 16 - b.getOffset(), null);
					}
				}
			}
		}
		bs.show();
		g.dispose();

	}

	private Image get(String name) {
		Image i = images.get(name);
		if (i == null) {
			try {
				i = ImageIO.read(new File(name));
			} catch (IOException e) {
				System.err.println("Error loading file: " + name);
				e.printStackTrace();
			}
		}
		return i;

	}
}
