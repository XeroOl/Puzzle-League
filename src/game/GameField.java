package game;

import java.util.Random;

import controller.GameInput;

/**
 * This is area where the blocks and stuff are!
 * 
 * @author Edison
 */
public class GameField {
	public static final int HEIGHT = 12;
	public static final int WIDTH = 6;
	public static final int TILESIZE = 16;
	/*------------------------------*OPTIONS*------------------------------*/
	private boolean explodelift = true;
	private int fallspeed = 1; // fallspeed -1 = instant
	private int blocktypecount = 5; //number of unique types of blocks
	private int maxtrashheight = 100; //number of lines of trash above top of screen before player loses
	private int stoptimermultiplier = 1; // 1 = normal, 0 = instakill, -1 = infinite time
	private int liftmultiplier = 16; // 0 = no lift, n = lift 1 in n frames
	private int trashbreakstrategy = 0; // 0 = default, 1 = like in nanoha puzzle league
	private boolean trashenabled = true;
	private int clearline = -1; // line to clear by, where -1 is disabled;
	private int timelimit = -1; // number of frames until gameover, where -1 is disabled
	private boolean multiplayertrashmetal = true; // trash sent by one player will not clear adjacent trash from other players
	/*------------------------------*CODE*------------------------------*/
	private static final Random r = new Random();
	private Block[][] board = new Block[HEIGHT][WIDTH];
	private int cx = WIDTH / 2 - 1, cy = HEIGHT / 2;
	private int mychain = 1;
	int stopframes = 0;
	int raiseframecounter = 0;
	int raiseprogress = 0;
	private boolean raise = true; // set to false if anything at all should stop the stack from raising
	private Block[] nextrow = new Block[WIDTH];

	public static class Builder {
		private GameField p = new GameField();

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

		public GameField build() {
			p.init();
			return p;
		}
	}

	private GameField() {
	}

	public void init() {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				board[y][x] = new Block();
				board[y][x].color = r.nextInt(blocktypecount);
			}
		}
		generateNextRow();
	}

	public void update(final GameInput input) {
		raise = true;
		cursor(input);
		swap(input);
		animateswap();
		animatematch(); // set above block's chain to true
		fall();
		match();
		resetchainflag(); // set ground block's chain to false, if there is no match nor block that is chain, tell trash() to send the trash;
		clearline();
		trash(); //sends trash, and adds sent trash
		lift(input);
	}

	private void cursor(GameInput input) {
		cx = input.cx;
		cy = input.cy;
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
				if (board[y][x].canSwap() && board[y][x].color != 0) {
					board[y][x].inair = (board[y][x].inair && board[y][x].offset > 0) || (y != HEIGHT - 1 && !board[y + 1][x].isSolid());
					if (board[y][x].inair) {
						raise = false;
						if (fallspeed == -1)
							board[y][x].offset -= TILESIZE;
						else {
							board[y][x].veldown += fallspeed;
							if (board[y][x].veldown > TILESIZE)
								board[y][x].veldown = TILESIZE;
							board[y][x].offset -= board[y][x].veldown;
						}
						if (board[y][x].offset < 0) {
							if (y != HEIGHT - 1 && !board[y + 1][x].isSolid()) {
								Block temp = board[y + 1][x];
								board[y + 1][x] = board[y][x];
								board[y][x] = temp;
								board[y + 1][x].offset += TILESIZE;
							} else {
								board[y][x].offset = 0;
								board[y][x].veldown = 0;
							}
						}
					}
				}
			}
		}
	}

	private void match() {
		boolean ischainmatch = false;
		boolean ismatch = false;
		for (int y = 1; y < HEIGHT - 1; y++) {
			for (int x = 0; x < WIDTH; x++) {
				//vertical
				if (board[y][x].canMatch() && board[y + 1][x].canMatch() && board[y - 1][x].canMatch() && board[y][x].equals(board[y + 1][x]) && board[y][x].equals(board[y - 1][x])) {
					ismatch = true;
					board[y][x].matchanimationframe = -2;
					board[y + 1][x].matchanimationframe = -2;
					board[y - 1][x].matchanimationframe = -2;
					ischainmatch |= board[y][x].chainpowered || board[y + 1][x].chainpowered || board[y - 1][x].chainpowered;
				}
			}
		}
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 1; x < WIDTH - 1; x++) {
				//horizontal
				if (board[y][x].canMatch() && board[y][x + 1].canMatch() && board[y][x - 1].canMatch() && board[y][x].equals(board[y][x + 1]) && board[y][x].equals(board[y][x - 1])) {
					ismatch = true;
					board[y][x].matchanimationframe = -2;
					board[y][x + 1].matchanimationframe = -2;
					board[y][x - 1].matchanimationframe = -2;
					ischainmatch |= board[y][x].chainpowered || board[y][x + 1].chainpowered || board[y][x - 1].chainpowered;
				}
			}
		}
		if (ismatch) {
			raise = false;
			if (ischainmatch) {
				mychain++;
			}
			int count = 0;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					//finalize matches
					if (board[y][x].matchanimationframe == -2) {

						board[y][x].matchid = count;
						board[y][x].chainnum = ischainmatch ? mychain : 1;
						count++;
					}
				}
			}
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (board[y][x].matchanimationframe == -2) {

						board[y][x].matchanimationframe = count * TILESIZE / 2 + 30;
					}
				}
			}
			if (count > 3) {
				System.out.println("Send trash because of +" + count + " combo");
			}
		}
	}

	private void animatematch() {
		int y2;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].matchanimationframe > 0) {
					raise = false;
					board[y][x].matchanimationframe--;
					if (board[y][x].matchanimationframe == 0) {
						board[y][x] = new Block();
						for (y2 = y - 1; y2 >= 0 && board[y2][x].canMatch(); y2--) {
							board[y2][x].chainpowered = true;
						}
					}
				}
			}
		}
	}

	private void resetchainflag() {
		boolean keep = false;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].canMatch() && (y == HEIGHT - 1 || !board[y + 1][x].inAnimation())) {
					board[y][x].chainpowered = false;
				} else {
					if (board[y][x].chainpowered) {
						keep = true;
					}
				}
			}
		}
		if (!keep) {
			if (mychain > 1) {
				System.out.println("send a trash because of x" + mychain + " chain");
			}
			mychain = 1;
		}
	}

	private void clearline() {

	}

	private void trash() {

	}

	private void lift(GameInput input) {
		if (input.raisingStack) {
			stopframes = 0;
			raiseprogress++;
		} else if (raise && liftmultiplier != 0) {
			if (stopframes > 0) {
				stopframes--;
			} else {
				raiseframecounter++;
				if (raiseframecounter > liftmultiplier) {
					raiseframecounter = 0;
					raiseprogress++;

				}
			}
		}
		if (raiseprogress == TILESIZE) {
			raiseprogress = 0;
			for (int x = 0; x < GameField.WIDTH; x++) {
				if (board[0][x].isSolid()) {
					//gameover 
					return;
				}
			}
			cy--;
			coordshifted = true;
			for (int y = 0; y < GameField.HEIGHT - 1; y++) {
				board[y] = board[y + 1];
			}
			board[GameField.HEIGHT - 1] = nextrow;
			generateNextRow();
		} else {
			coordshifted = false;
		}
	}

	private void generateNextRow() {
		nextrow = new Block[GameField.WIDTH];
		for (int x = 0; x < GameField.WIDTH; x++) {
			nextrow[x] = new Block();
			nextrow[x].color = 1 + r.nextInt(blocktypecount - 1);
		}
	}

	public int getCursorX() {
		return cx;
	}

	public int getCursorY() {
		return cy;
	}

	boolean coordshifted = false;

	public boolean adjustY() {
		return coordshifted;
	}

	public Block blockAt(int x, int y) {
		return board[y][x];
	}

	public int getRaiseProgress() {
		return raiseprogress;
	}

}
