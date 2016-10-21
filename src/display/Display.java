package display;

import game.Block;
import game.GameField;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Display extends Canvas {
	public static double scale = 2.0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 9039285670020804667L;
	BufferStrategy bs;
	int theme = 1;
	String themepath = "assets/theme" + theme + "/";
	static Map<String, Image> images = new HashMap<String, Image>();

	public Display() {
	}

	public void update(GameField p) {
		if (bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
			if (bs == null)
				return;
		}
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g.scale(scale, scale);

		g.drawImage(get(themepath + "background1.png"), 0, 0, null);

		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; y < GameField.HEIGHT; y++) {
				Block b = p.blockAt(x, y);
				if (b.getColor() != 0) {
					if (b.isTrash()) {
						//ignore it for now
					} else if (!b.inMatchAnimation()) {
						g.drawImage(get(themepath + "block" + b.getColor() + ".png"), x * GameField.TILESIZE + b.getSwapAnim() * 2, y * GameField.TILESIZE - b.getOffset() - p.getRaiseProgress(), null);
					}
				}
			}
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; y < GameField.HEIGHT; y++) {
				Block b = p.blockAt(x, y);
				if (b.getColor() != 0) {
					if (b.inMatchAnimation()) {
						g.drawImage(get(themepath + "block" + b.getColor() + ".png"), x * GameField.TILESIZE + GameField.TILESIZE / 2 - b.getMatchAnimationFrame(), y * GameField.TILESIZE + GameField.TILESIZE / 2 - b.getMatchAnimationFrame() - p.getRaiseProgress(), 2 * b.getMatchAnimationFrame(), 2 * b.getMatchAnimationFrame(), null);
						g.drawImage(get(themepath + "match" + b.getChainNum() + ".png"), x * 16, y * 16 - p.getRaiseProgress(), null);
					}
				}
			}
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g.drawImage(get(themepath + "cursor.png"), p.getCursorX() * GameField.TILESIZE - GameField.TILESIZE, p.getCursorY() * GameField.TILESIZE - GameField.TILESIZE - p.getRaiseProgress(), null);

		bs.show();
		g.dispose();

	}

	private Image get(String name) {
		Image i = images.get(name);
		if (i == null) {
			try {
				i = ImageIO.read(new File(name));
				images.put(name, i);
			} catch (IOException e) {
				System.err.println("Error loading file: " + name);
				e.printStackTrace();
			}
		}
		return i;

	}
}
