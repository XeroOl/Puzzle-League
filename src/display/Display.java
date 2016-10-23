package display;

import game.Block;
import game.GameField;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.javafx.animation.TickCalculation;

import static game.GameField.TILESIZE;

public class Display extends Canvas {
	public static double scale = 2.0;
	/**
	 * 
	 */
	public static int OFFSET_X = 3;
	public static int OFFSET_Y = 1;
	public static int STATUS_OFFSET_X = 1;
	private static final long serialVersionUID = 9039285670020804667L;
	BufferStrategy bs;
	int theme = 1;
	static final int BLOCK_THEME_COUNT = 1;
	static Image[] BLOCK_SHEET = new Image[BLOCK_THEME_COUNT];
	static final int PLAYER_THEME_COUNT = 1;
	static Image[] PLAYER_SHEET = new Image[PLAYER_THEME_COUNT];
	static final int GUI_THEME_COUNT = 1;
	static Image[] GUI_SHEET = new Image[GUI_THEME_COUNT];
	String path = "assets/";

	public Display() {
		setPreferredSize(new Dimension((int) (GameField.WIDTH * TILESIZE * scale), (int) (GameField.HEIGHT * TILESIZE * scale)));
	}

	public void update(GameField gf) {
		if (bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
			if (bs == null)
				return;
		}
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g.scale(scale, scale);

		drawBackground(g, gf);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		drawTiles(g, gf);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f));
		darkenTiles(g, gf);
		fixGarbageTiles(g, gf);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
		drawMatchingTiles(g, gf);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		drawSurroundings(g, gf);
		drawCursor(g, gf);
		drawStatusBars(g, gf);
		bs.show();
		g.dispose();

	}

	private void drawBackground(Graphics2D g, GameField gf) {
		drawGUI(g, OFFSET_X * TILESIZE, OFFSET_Y * TILESIZE, GameField.WIDTH * TILESIZE, GameField.HEIGHT * TILESIZE, backgroundx + OFFSET_X, backgroundy + OFFSET_Y, GameField.WIDTH, GameField.HEIGHT);
	}

	private void drawTiles(Graphics2D g, GameField gf) {
		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; y <= GameField.HEIGHT; y++) {
				Block b = gf.blockAt(x, y);
				if (b.isTrash()) {
					drawPlayer(g, x * TILESIZE + TILESIZE * OFFSET_X, y * TILESIZE - b.getOffset() * TILESIZE / gf.getFallSpeedDivisor() - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, getPlayerX(b.getColor()), getPlayerY(b.getColor()));
				} else if (b.getColor() != 0 && !b.inMatchAnimation()) {
					drawBlock(g, x * TILESIZE + b.getSwapAnim() * 2 + TILESIZE * OFFSET_X, y * TILESIZE - b.getOffset() * TILESIZE / gf.getFallSpeedDivisor() - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, getBlockX(b.getColor()), getBlockY(b.getColor()));

				}
			}
		}

	}

	private void darkenTiles(Graphics2D g, GameField gf) {

		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; gf.isGameOver() && y <= GameField.HEIGHT; y++)
				drawBlock(g, (x + OFFSET_X) * TILESIZE, (y + OFFSET_Y) * TILESIZE - gf.getRaiseProgress(), getBlockX(31), getBlockY(31));
		}
	}

	private void fixGarbageTiles(Graphics2D g, GameField gf) {
		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; y <= GameField.HEIGHT; y++) {
				Block b = gf.blockAt(x, y);
				if (b.isTrash()) {
					drawPlayer(g, x * TILESIZE + TILESIZE * OFFSET_X, y * TILESIZE - b.getOffset() * TILESIZE / gf.getFallSpeedDivisor() - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, getTrashX(b.getTrashType()), getTrashY(b.getTrashType()));
				}
			}
		}
	}

	private void drawMatchingTiles(Graphics2D g, GameField gf) {
		for (int x = 0; x < GameField.WIDTH; x++) {
			for (int y = 0; y < GameField.HEIGHT; y++) {
				Block b = gf.blockAt(x, y);
				if (b.getColor() != 0) {
					if (b.inMatchAnimation()) {
						drawBlock(g, x * TILESIZE + TILESIZE / 2 - b.getMatchAnimationFrame() + TILESIZE * OFFSET_X, y * TILESIZE + TILESIZE / 2 - b.getMatchAnimationFrame() - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, 2 * b.getMatchAnimationFrame(), 2 * b.getMatchAnimationFrame(), getBlockX(b.getColor()), getBlockY(b.getColor()), 1, 1);
						drawBlock(g, x * TILESIZE + TILESIZE * OFFSET_X, y * TILESIZE - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, getMatchX(b.getChainNum()), getMatchY(b.getChainNum()));
					}
				}
			}
		}
	}

	private void drawCursor(Graphics2D g, GameField gf) {
		drawGUI(g, gf.getCursorX() * TILESIZE - TILESIZE + TILESIZE * OFFSET_X, gf.getCursorY() * TILESIZE - TILESIZE - gf.getRaiseProgress() + TILESIZE * OFFSET_Y, cursorwidth * TILESIZE, cursorheight * TILESIZE, getCursorX(0), getCursorY(0), cursorwidth, cursorheight);//get(themepath + "cursor.png"),
	}

	private void drawSurroundings(Graphics2D g, GameField gf) {
		drawGUI(g, 0, 0, (OFFSET_X * 2 + GameField.WIDTH) * TILESIZE, OFFSET_Y * TILESIZE, 0, 0, OFFSET_X * 2 + GameField.WIDTH, OFFSET_Y);
		drawGUI(g, 0, OFFSET_Y * TILESIZE, OFFSET_X * TILESIZE, GameField.HEIGHT * TILESIZE, 0, OFFSET_Y, OFFSET_X, GameField.HEIGHT);
		drawGUI(g, (OFFSET_X + GameField.WIDTH) * TILESIZE, OFFSET_Y * TILESIZE, OFFSET_X * TILESIZE, GameField.HEIGHT * TILESIZE, OFFSET_X + GameField.WIDTH, OFFSET_Y, OFFSET_X, GameField.HEIGHT);
		drawGUI(g, 0, (OFFSET_Y + GameField.HEIGHT) * TILESIZE, (OFFSET_X * 2 + GameField.WIDTH) * TILESIZE, OFFSET_Y * TILESIZE, 0, OFFSET_Y + GameField.HEIGHT, OFFSET_X * 2 + GameField.WIDTH, OFFSET_Y);
	}

	private void drawStatusBars(Graphics2D g, GameField gf) {
		g.drawImage(getGUISheet(), (OFFSET_X - STATUS_OFFSET_X - 1) * TILESIZE, (OFFSET_Y + GameField.HEIGHT) * TILESIZE - (gf.getGarbageAmount()) * TILESIZE / 4, (OFFSET_X - STATUS_OFFSET_X) * TILESIZE, (OFFSET_Y + GameField.HEIGHT) * TILESIZE, garbagemeterx * TILESIZE, (garbagemetery + GameField.HEIGHT) * TILESIZE - (gf.getGarbageAmount()) * TILESIZE / 4, (garbagemeterx + 1) * TILESIZE, (garbagemetery + GameField.HEIGHT) * TILESIZE, null);
		g.drawImage(getGUISheet(), (OFFSET_X + GameField.WIDTH + STATUS_OFFSET_X) * TILESIZE, (OFFSET_Y + GameField.HEIGHT) * TILESIZE - (gf.getStopTime()) * TILESIZE / 4, (OFFSET_X + GameField.WIDTH + STATUS_OFFSET_X + 1) * TILESIZE, (OFFSET_Y + GameField.HEIGHT) * TILESIZE, stopmeterx * TILESIZE, (stopmetery + GameField.HEIGHT) * TILESIZE - (gf.getStopTime()) * TILESIZE / 4, (stopmeterx + 1) * TILESIZE, (stopmetery + GameField.HEIGHT) * TILESIZE, null);

	}

	private Image getBlockSheet() {
		if (BLOCK_SHEET[theme - 1] == null) {
			try {
				BLOCK_SHEET[theme - 1] = ImageIO.read(new File(path + "block/block" + theme + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return BLOCK_SHEET[theme - 1];

	}

	private Image getPlayerSheet() {
		if (PLAYER_SHEET[theme - 1] == null) {
			try {
				PLAYER_SHEET[theme - 1] = ImageIO.read(new File(path + "player/player" + theme + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return PLAYER_SHEET[theme - 1];

	}

	private Image getGUISheet() {
		if (GUI_SHEET[theme - 1] == null) {
			try {
				GUI_SHEET[theme - 1] = ImageIO.read(new File(path + "gui/gui" + theme + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return GUI_SHEET[theme - 1];

	}

	private void drawGUI(Graphics2D g, int x, int y, int width, int height, int sx, int sy, int swidth, int sheight) {
		draw(getGUISheet(), g, x, y, width, height, sx, sy, swidth, sheight);
	}

	private void drawGUI(Graphics2D g, int x, int y, int sx, int sy) {
		draw(getGUISheet(), g, x, y, sx, sy);
	}

	private void drawBlock(Graphics2D g, int x, int y, int width, int height, int sx, int sy, int swidth, int sheight) {
		draw(getBlockSheet(), g, x, y, width, height, sx, sy, swidth, sheight);
	}

	private void drawBlock(Graphics2D g, int x, int y, int sx, int sy) {
		draw(getBlockSheet(), g, x, y, sx, sy);
	}

	private void drawPlayer(Graphics2D g, int x, int y, int width, int height, int sx, int sy, int swidth, int sheight) {
		draw(getPlayerSheet(), g, x, y, width, height, sx, sy, swidth, sheight);
	}

	private void drawPlayer(Graphics2D g, int x, int y, int sx, int sy) {
		draw(getPlayerSheet(), g, x, y, sx, sy);
	}

	private void draw(Image i, Graphics2D g, int x, int y, int width, int height, int sx, int sy, int swidth, int sheight) {
		g.drawImage(i, x, y, x + width, y + height, sx * TILESIZE, sy * TILESIZE, (sx + swidth) * TILESIZE, (sy + sheight) * TILESIZE, null);
	}

	private void draw(Image i, Graphics2D g, int x, int y, int sx, int sy) {
		g.drawImage(i, x, y, x + TILESIZE, y + TILESIZE, sx * TILESIZE, sy * TILESIZE, sx * TILESIZE + TILESIZE, sy * TILESIZE + TILESIZE, null);
	}

	/************SHEET PARSING VARIABLES AND METHODS*************/
	static int backgroundx = 0;
	static int backgroundy = 0;
	static int cursorwidth = 4;
	static int cursorheight = 3;
	static int garbagemeterx = 13;
	static int garbagemetery = 1;
	static int stopmeterx = 16;
	static int stopmetery = 1;

	private static int getBlockX(int blockid) {
		return blockid % 8;
	}

	private static int getBlockY(int blockid) {
		return blockid / 8;
	}

	private static int getMatchX(int id) {
		return id <= 32 ? (id - 1) % 8 : 7;
	}

	private static int getMatchY(int id) {
		return id <= 32 ? (id - 1) / 8 + 4 : 3;
	}

	private static int getCursorX(int id) {
		return 28;
	}

	private static int getCursorY(int id) {
		return 2;
	}

	private static int getPlayerX(int id) {
		return 3 + id % 4;
	}

	private static int getPlayerY(int id) {
		return 3 + id / 4;
	}

	private static int getTrashX(int id) {
		return id % 3;
	}

	private static int getTrashY(int id) {
		return id / 3;
	}
}
