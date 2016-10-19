package game;

import java.util.Random;

import display.GameInput;

/**
 * This is area where the blocks and stuff are!
 * @author Edison
 */
public class Player {
	public static final int HEIGHT = 12;
	public static final int WIDTH = 6;
	/*------------------------------*OPTIONS*------------------------------*/
	private boolean explodelift = true;
	private int fallspeed = -1; // fallspeed -1 = instant
	private int blocktypecount = 5; //number of unique types of blocks
	private int maxtrashheight = 100; //number of lines of trash above top of screen before player loses
	private int stoptimermultiplier = 1; // 1 = normal, 0 = instakill, -1 = infinite time
	private int liftmultiplier = 0; // 0 = no lift, -n = lift n per frame, n = lift n+f per frame
	private int trashbreakstrategy = 0; // 0 = default, 1 = like in nanoha puzzle league
	private boolean trashenabled = true;
	private int clearline = -1; // line to clear by, where -1 is disabled;
	private int timelimit = -1; // number of frames until gameover, where -1 is disabled
	private boolean multiplayertrashmetal = true; // trash sent by one player will not clear adjacent trash from other players
	/*------------------------------*CODE*------------------------------*/
	private static final Random r = new Random();
	private Block[][] board = new Block[HEIGHT][WIDTH];
	private int cx = WIDTH / 2 - 1, cy = HEIGHT / 2;
	private int mychain = 0;
	private boolean raise = true; // set to false if anything at all should stop the stack from raising

	public static class Builder {
		private Player p = new Player();

		public Builder setExplodeLift(boolean explodelift) {
			p.explodelift = explodelift;
			return this;
		}

		public Builder fallSpeed(int fallspeed) {
			p.fallspeed = fallspeed;
			return this;
		}

		public Builder blockTypeCount(int blocktypecount) {
			p.blocktypecount = blocktypecount;
			return this;
		}

		public Builder maxTrashHeight(int maxtrashheight) {
			p.maxtrashheight = maxtrashheight;
			return this;
		}

		public Builder stopTimerMultiplier(int stoptimermultiplier) {
			p.stoptimermultiplier = stoptimermultiplier;
			return this;
		}

		public Builder liftMultiplier(int liftmultiplier) {
			p.liftmultiplier = liftmultiplier;
			return this;
		}

		public Builder trashBreakStrategy(int trashbreakstrategy) {
			p.trashbreakstrategy = trashbreakstrategy;
			return this;
		}

		public Builder trashEnabled(boolean trashenabled) {
			p.trashenabled = trashenabled;
			return this;
		}

		public Builder clearline(int clearline) {
			p.clearline = clearline;
			return this;
		}

		public Builder timeLimit(int timelimit) {
			p.timelimit = timelimit;
			return this;
		}

		public Builder multiplayerTrashMetal(boolean multiplayertrashmetal) {
			p.multiplayertrashmetal = multiplayertrashmetal;
			return this;
		}

		public Player build() {
			p.init();
			return p;
		}
	}

	private Player() {
	}

	public void init() {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				board[y][x] = new Block();
				board[y][x].color = r.nextInt(6);
			}
		}
	}

	public void update(final GameInput input) {
		raise = true;
		cursor(input);
		swap(input);
		animateswap();
		fall();
		match();
		animatematch(); // set above block's chain to true
		resetchainflag(); // set ground block's chain to false, if there is no match nor block that is chain, tell trash() to send the trash;
		clearline();
		trash(); //sends trash, and adds sent trash
		lift(input);
	}

	private void cursor(GameInput input) {
		if (input.left && cx > 0)
			cx--;
		if (input.right && cx < board.length - 2)
			cx++;
		if (input.down && cy > 0)
			cy--;
		if (input.up && cy < board[0].length - 1)
			cy++;
	}

	public Block blockAt(int x, int y) {
		return board[y][x];
	}

	private void swap(GameInput input) {
		if (input.swapping) {
			if (board[cy][cx].canSwap() && board[cy][cx + 1].canSwap()) {
				board[cy][cx].swapAnim = 4;
				board[cy][cx + 1].swapAnim = -4;
			}
		}
	}

	private void animateswap() {
		Block temp = null;
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (board[y][x].swapAnim != 0) {
					if (board[y][x].swapAnim > 0) {
						board[y][x].swapAnim--;
						if (board[y][x].swapAnim == 2) {
							temp = board[y][x];//save this block
						}
					} else {
						board[y][x].swapAnim++;
						if (board[y][x].swapAnim == -2) {
							board[y][x - 1] = board[y][x];
							board[y][x - 1].swapAnim = 2;
							board[y][x] = temp;//put it in the other slot
							board[y][x].swapAnim = -2;
						}
					}
				}
			}
		}

	}

	private void fall() {
		for (int y = HEIGHT - 1; y >= 0; y--) {
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].canSwap()) {
					board[y][x].inair = (board[y][x].inair && board[y][x].offset > 0) || (y != HEIGHT - 1 && board[y + 1][x].color == 0);
					if (board[y][x].inair) {
						board[y][x].offset--;
						if (board[y][x].offset < 0) {
							if (y != HEIGHT - 1 && board[y + 1][x].color == 0) {
								Block temp = board[y + 1][x];
								board[y + 1][x] = board[y][x];
								board[y][x] = temp;
								board[y + 1][x].offset += 16;
							}
						}
					}
				}
			}
		}
	}

	private void match() {

	}

	private void animatematch() {
		// TODO Auto-generated method stub

	}

	private void resetchainflag() {

	}

	private void clearline() {

	}

	private void trash() {

	}

	private void lift(GameInput input) {

	}

}
