package game;

import java.util.ArrayDeque;
import java.util.Queue;
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
	private int fallspeed = 32; // fallspeed 4 = beginner 32 = normal, 64 = hard, -1 = instant
	private int colorcount = 5; //number of unique colors of blocks (up to 9)
	private int maxtrashheight = 100; //number of lines of trash above top of screen before player loses
	private int stoptimermultiplier = 10; // 10 = normal, 0 = instakill, -1 = infinite time
	private int liftmultiplier = 16; // 0 = no lift, n = lift 1 in n frames
	private int trashbreakstrategy = 0; // 0 = default, 1 = like in nanoha puzzle league
	private boolean trashenabled = true;
	private int clearline = -1; // line to clear by, where -1 is disabled;
	private int timelimit = -1; // number of frames until gameover, where -1 is disabled
	private boolean multiplayertrashmetal = true; // trash sent by one player will not clear adjacent trash from other players
	private boolean combotrashontop = true; // when a player makes a combo during a chain, should the trash for the combo be saved until after the chain trash is sent?
	/*------------------------------*NON-OPTIONS*------------------------------*/
	private int stoptimecombo = 1;
	private int stoptimechain = 4;
	private int stoptimebuffer = 5;
	private int stoptimemax = 960;
	private int blockstartheight = 5;
	private int fallspeeddivisor = 1024;
	private int player = 1;
	private boolean processoffscreentrash = false;
	/*------------------------------*CODE*------------------------------*/
	private static final Random r = new Random();
	private Block[][] board = new Block[HEIGHT][WIDTH];
	private int cx = WIDTH / 2 - 1, cy = HEIGHT / 2;
	private int mychain = 1;
	private int stopframes = 0;
	private int raiseframecounter = 0;
	private int raiseprogress = 0;
	private boolean raise = true; // set to false if anything at all should stop the stack from raising
	private boolean forcelift = false;
	private Block[] nextrow = new Block[WIDTH];
	private boolean oddframe = false;
	boolean gameover = false;
	Queue<Integer> combos = new ArrayDeque<Integer>();
	Queue<Trash> mytrash = new ArrayDeque<Trash>();

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
			if (blocktypecount > 1)
				p.colorcount = blocktypecount;
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

		public Builder comboTrashOnTop(boolean combotrashontop) {
			p.combotrashontop = combotrashontop;
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
			}
		}
		generateNextRow();
		for (int h = 0; h < blockstartheight; h++) {
			for (int y = 0; y < HEIGHT - 1; y++) {
				board[y] = board[y + 1];
			}
			board[HEIGHT - 1] = nextrow;
			generateNextRow();
		}
	}

	public void update(final GameInput input) {
		if (!gameover) {
			oddframe ^= true;
			raise = true;
			cursor(input);
			swap(input);
			animateswap();
			animatematch(); // set above block's chain to true
			fall();
			match();
			checkchains(); // set ground block's chain to false, if there is no match nor block that is chain, tell trash() to send the trash;
			clearline();
			trash(); //sends trash, and adds sent trash
			lift(input);
		} else {
			fall();
		}
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
					board[y][x].veldown = 0;
					board[y][x].offset = 0;
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
					if (board[y][x].swapAnim == 0) {
						board[y][x].removechainpower = true;
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
							board[y][x].offset -= fallspeeddivisor;
						else {
							board[y][x].veldown += fallspeed;
							if (board[y][x].veldown > fallspeeddivisor)
								board[y][x].veldown = fallspeeddivisor;
							board[y][x].offset -= board[y][x].veldown;

						}
						if (y != HEIGHT - 1 && board[y + 1][x].isSolid() && board[y + 1][x].offset > board[y][x].offset && board[y][x].veldown >= board[y + 1][x].veldown) {
							board[y][x].offset = board[y + 1][x].offset;
							board[y][x].veldown = board[y + 1][x].veldown;
						}
						if (board[y][x].offset < 0) {
							if (y != HEIGHT - 1 && !board[y + 1][x].isSolid()) {
								Block temp = board[y + 1][x];
								board[y + 1][x] = board[y][x];
								board[y][x] = temp;
								board[y + 1][x].offset += fallspeeddivisor;
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
				stopframes += stoptimermultiplier * stoptimecombo * count;
				combos.add(count > 6 ? 6 : count - 1);
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

	private void checkchains() {
		boolean keep = false;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].canMatch() && (y == HEIGHT - 1 || !board[y + 1][x].inSwapAnimation()) || board[y][x].removechainpower) {
					board[y][x].chainpowered = false;
					board[y][x].removechainpower = false;
				} else {
					if (board[y][x].chainpowered) {
						keep = true;
					}
				}
			}
		}
		if (!keep) {
			if (mychain > 1) {
				stopframes += stoptimermultiplier * stoptimechain * mychain;
				System.out.println("send a trash because of x" + mychain + " chain");
			}
			mychain = 1;
		}
		while (!combotrashontop && !combos.isEmpty()) {
			Trash t = new Trash();
			combos.poll();

		}
	}

	private void clearline() {

	}

	private void trash() {

	}

	private void lift(GameInput input) {
		if (forcelift || input.raisingStack && (explodelift || raise) && !touchingTop()) {
			stopframes = 0;
			raiseprogress++;
			forcelift = true;
		} else if (raise && liftmultiplier > 0 && stoptimermultiplier != -1) {
			if (stopframes > 0) {
				stopframes--;
			} else {
				raiseframecounter++;
				if (touchingTop()) {
					gameover = true;
					return;
				}
				if (raiseframecounter > liftmultiplier) {
					raiseframecounter = 0;
					raiseprogress++;

				}
			}
		}
		if (raiseprogress == TILESIZE) {
			raiseprogress = 0;
			forcelift = false;

			if (touchingTop()) {
				//gameover 
				gameover = true;
				return;
			}

			cy--;
			coordshifted = true;
			for (int y = 0; y < HEIGHT - 1; y++) {
				board[y] = board[y + 1];
			}
			board[HEIGHT - 1] = nextrow;
			generateNextRow();
			if (touchingTop()) {
				stopframes += stoptimebuffer * stoptimermultiplier;
			}
		} else {
			coordshifted = false;
		}
	}

	private boolean touchingTop() {
		for (int x = 0; x < WIDTH; x++) {
			if (board[0][x].isSolid()) {
				return true;
			}
		}
		return false;
	}

	private void generateNextRow() {
		boolean[][] map = new boolean[WIDTH][colorcount];
		nextrow = new Block[WIDTH];
		for (int x = 0; x < WIDTH; x++) {
			nextrow[x] = new Block();
		}
		for (int x = 0; x < WIDTH; x++) {
			if (board[HEIGHT - 1][x].canMatch() && board[HEIGHT - 2][x].canMatch() && board[HEIGHT - 1][x].color == board[HEIGHT - 2][x].color) {
				map[x][board[HEIGHT - 1][x].color - 1] = true;
			}
		}
		boolean changehappened = false;
		int collapsed = 0;
		do {
			do {
				changehappened = false;
				//collapse anything collapsable
				for (int x = 0; x < WIDTH; x++) {
					if (nextrow[x].color == 0) {
						int val = indexofonlyfalse(map[x]);
						if (val != -1) {
							nextrow[x].color = val + 1;
							changehappened = true;
							collapsed++;
						}
					}
				}
				//ban 3 in a rows
				loop: for (int x = 0; x < WIDTH - 2; x++) {
					int color = 0;
					int encountered = 0;
					for (int x2 = x; x2 < x + 3; x2++) {
						if (nextrow[x2].color != 0) {
							if (color == 0)
								color = nextrow[x2].color;
							else if (color != nextrow[x2].color)
								continue loop;
							encountered++;
						}
					}
					if (encountered == 2) {

						for (int x2 = x; x2 < x + 3; x2++) {
							if (!map[x2][color - 1]) {
								changehappened = true;
								map[x2][color - 1] = true;
							}
						}
					}
				}
			} while (changehappened);
			if (collapsed == WIDTH) {
				break;
			}
			int nextcollapse = r.nextInt(WIDTH - collapsed);
			out: for (int x = 0; x < WIDTH; x++) {
				if (nextrow[x].color == 0) {
					if (nextcollapse == 0) {
						int val = r.nextInt(falsecount(map[x]));
						for (int y = 0; y < colorcount; y++) {
							if (!map[x][y]) {
								if (val == 0) {
									nextrow[x].color = y + 1;
									collapsed++;
									break out;
								} else {
									val--;
								}
							}
						}
					} else {
						nextcollapse--;
					}
				}
			}
		} while (collapsed < WIDTH);
	}

	static int falsecount(boolean[] b) {
		int count = 0;
		for (int i = 0; i < b.length; i++) {
			if (!b[i])
				count++;
		}
		return count;
	}

	static int indexofonlyfalse(boolean[] b) {

		int i;
		for (i = 0; i < b.length && b[i]; i++)
			;
		int index = i;
		if (i == b.length)
			return -1;
		for (i++; i < b.length && b[i]; i++)
			;
		if (i != b.length)
			return -1;
		return index;

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
		return y == HEIGHT ? nextrow[x] : board[y][x];
	}

	public int getRaiseProgress() {
		return raiseprogress;
	}

	public int getGarbageAmount() {
		return mytrash.size() * 48 / maxtrashheight;
	}

	public int getStopTime() {
		return stopframes * 48 / stoptimemax;
	}

	public int getFallSpeedDivisor() {
		return fallspeeddivisor;
	}

}
