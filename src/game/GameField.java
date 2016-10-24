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
	private int trashbreakstrategy = 0; // 0 = default, 1 = like in nanoha puzzle league, 2 = all into blocks
	private boolean trashenabled = true;
	private int clearline = -1; // line to clear by, where -1 is disabled;
	private int timelimit = -1; // number of frames until gameover, where -1 is disabled
	private boolean multiplayertrashmetal = true; // trash sent by one player will not clear adjacent trash from other players
	private boolean combotrashontop = true; // when a player makes a combo during a chain, should the trash for the combo be saved until after the chain trash is sent?
	/*------------------------------*NON-OPTIONS*------------------------------*/
	private int stoptimecombo = 1;
	private int stoptimechain = 10;
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
	private Block[] trashrow = new Block[WIDTH];
	private boolean trashrowvalid = false;
	private boolean gameover = false;
	Queue<Trash> trashoutput = new ArrayDeque<Trash>();
	Queue<Trash> combotrashoutput = new ArrayDeque<Trash>();
	ArrayDeque<Trash> trashinput = new ArrayDeque<Trash>();

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
		trashinput.add(new Trash(3, 6, 2));
		trashinput.add(new Trash(2, 6, 2));
		trashinput.add(new Trash(1, 6, 2));
	}

	public void update(final GameInput input) {
		if (!gameover) {
			raise = true;
			cursor(input);
			swap(input);
			animateswap();
			animatematch(); // set above block's chain to true
			fall();
			match();
			checkchains(); // set ground block's chain to false, if there is no match nor block that is chain, tell trash() to send the trash;
			clearline();

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
			// Don't swap two air blocks!! they would become solid, which can gameover you if you swap air at the top of the screen
			if (board[cy][cx].canSwap() && board[cy][cx + 1].canSwap() && (board[cy][cx].color != 0 || board[cy][cx + 1].color != 0)) {
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
			//Calculate fall amount and offset
			for (int x = 0; x < WIDTH; x++) {
				if (!board[y][x].inAnimation() && (board[y][x].trash || board[y][x].color != 0)) {
					board[y][x].inair = (board[y][x].offset > 0) || (y != HEIGHT - 1 && !(board[y + 1][x].isSolid() && !board[y + 1][x].inair));
					if (board[y][x].inair) {
						if (fallspeed == -1)
							board[y][x].offset -= fallspeeddivisor;
						else {
							board[y][x].veldown += fallspeed;
							if (board[y][x].veldown > fallspeeddivisor)
								board[y][x].veldown = fallspeeddivisor;
							board[y][x].offset -= board[y][x].veldown;

						}
						if (y != HEIGHT - 1 && board[y + 1][x].isSolid() && board[y + 1][x].inair && board[y + 1][x].offset > board[y][x].offset && board[y][x].veldown >= board[y + 1][x].veldown) {
							board[y][x].offset = board[y + 1][x].offset;
							board[y][x].veldown = board[y + 1][x].veldown;
						}
						if (board[y][x].offset < 0 && (y == HEIGHT - 1 || board[y + 1][x].isSolid())) {
							board[y][x].inair = false;
							board[y][x].offset = 0;
							board[y][x].veldown = 0;
						}
					}
				}
			}
			//sync movement across trash
			for (int x = 0; x < WIDTH; x++) {
				if (!board[y][x].inAnimation() && board[y][x].trash && board[y][x].trashtype % 3 == 0) {
					//we've found the start of a trash block! iterate through to find the correct synchronized values, and store them in board[y][x]

					for (int x2 = x + 1; x2 < WIDTH; x2++) {
						board[y][x].inair &= board[y][x2].inair;
						board[y][x].offset = board[y][x].offset >= board[y][x2].offset ? board[y][x].offset : board[y][x2].offset;
						board[y][x].veldown = board[y][x].veldown <= board[y][x2].veldown ? board[y][x].veldown : board[y][x2].veldown;
						if (board[y][x2].trashtype % 3 == 2)
							break;
					}//distribute correct values to all of trash
					for (int x2 = x + 1; x2 < WIDTH; x2++) {
						board[y][x2].inair = board[y][x].inair;
						board[y][x2].offset = board[y][x].offset;
						board[y][x2].veldown = board[y][x].veldown;
						if (board[y][x2].trashtype % 3 == 2)
							break;

					}
				}
			}
			//move blocks down if neccesary
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].inair) {
					raise = false;
				}
				if (board[y][x].offset < 0) {
					Block temp = board[y + 1][x];
					board[y + 1][x] = board[y][x];
					board[y][x] = temp;
					board[y + 1][x].offset += fallspeeddivisor;
				}
			}
		}//move trash from trashrow
		trash(); //load in next trash row
		if (trashenabled && trashrowvalid && !touchingTop()) {
			for (int x = 0; x < WIDTH; x++) {
				if (trashrow[x].trash) {
					board[0][x] = trashrow[x];
					if (fallspeed == -1) {

					} else {
						if (board[0][x].veldown != 0)
							board[0][x].offset = board[1][x].offset;
						else {
							board[0][x].offset = fallspeeddivisor;
						}
					}
				}
			}
			trashrowvalid = false;
		}

	}

	private void match() {
		boolean ischainmatch = false;
		boolean ismatch = false;
		/**************VERTICAL DETECT PHASE*************/
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
		/**************HORIZONTAL DETECT PHASE*************/
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
			/**************BLOCK MATCH COUNT PHASE*************/
			raise = false;
			if (ischainmatch) {
				mychain++;
				stopframes += stoptimermultiplier * stoptimechain;
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
			/**************TRASH DETECT PHASE*************/
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (board[y][x].matchanimationframe == -2) {
						if (x != 0 && board[y][x - 1].isTrash() && board[y][x - 1].inMatchAnimation() == false && !board[y][x - 1].inair)
							board[y][x - 1].matchanimationframe = -2;
						if (x != WIDTH - 1 && board[y][x + 1].isTrash() && board[y][x + 1].inMatchAnimation() == false && !board[y][x + 1].inair)
							board[y][x + 1].matchanimationframe = -2;
						if (y != 0 && board[y - 1][x].isTrash() && board[y - 1][x].inMatchAnimation() == false && !board[y - 1][x].inair)
							board[y - 1][x].matchanimationframe = -2;
						if (y != HEIGHT - 1 && board[y + 1][x].isTrash() && board[y + 1][x].inMatchAnimation() == false && !board[y + 1][x].inair)
							board[y + 1][x].matchanimationframe = -2;
					}
				}
			}
			/**************TRASH SPREAD PHASE*************/
			int trashcount = 0;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (board[y][x].isTrash() && board[y][x].matchanimationframe == -2) {
						trashcount += spreadmatchtrash(x, y);
					}
				}
			}
			/**************TRASH FINALIZE PHASE*************/
			int t2 = 0;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (board[y][x].matchanimationframe == -3) {
						board[y][x].matchanimationframe = trashcount * TILESIZE / 2 + 30;
						board[y][x].matchid = t2++;
						board[y][x].chainpowered = true;
						switch (trashbreakstrategy) {
						case 0:
							board[y][x].dissappear = board[y][x].trashtype / 3 == 0 || board[y][x].trashtype / 3 == 3;
							break;
						case 1:
							//IDK how to do nanoka
						case 2:
							board[y][x].dissappear = true;
							break;
						}

					}
				}
			}
			/**************BLOCK FINALIZE PHASE*************/
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					if (board[y][x].matchanimationframe == -2) {
						board[y][x].matchanimationframe = count * TILESIZE / 2 + 30;
						board[y][x].dissappear = true;
					}
				}
			}
			if (count > 3) {
				stopframes += stoptimermultiplier * stoptimecombo * count;
				combotrashoutput.add(new Trash(0, count > WIDTH ? WIDTH : count - 1, player));
			}

		}
	}

	/**
	 * only called from the match() method. don't call in update order
	 */
	private int spreadmatchtrash(int x, int y) {
		if (board[y][x].matchanimationframe != -3) {
			board[y][x].matchanimationframe = -3;
			int count = 1;
			if (x != 0 && (board[y][x].trashtype % 3 != 0 || (board[y][x - 1].trash && (multiplayertrashmetal ? board[y][x - 1].color == board[y][x].color : board[y][x - 1].color == 0)))) {
				count += spreadmatchtrash(x - 1, y);
			}
			if (x != WIDTH - 1 && (board[y][x].trashtype % 3 != 2 || (board[y][x + 1].trash && (multiplayertrashmetal ? board[y][x + 1].color == board[y][x].color : board[y][x + 1].color == 0)))) {
				count += spreadmatchtrash(x + 1, y);
			}
			if (y != HEIGHT - 1 && ((board[y][x].trashtype / 3 != 0 && board[y][x].trashtype / 3 != 3) || (board[y + 1][x].trash && (multiplayertrashmetal ? board[y + 1][x].color == board[y][x].color : board[y + 1][x].color == 0)))) {
				count += spreadmatchtrash(x, y + 1);
			}
			if (y != 0 && ((board[y][x].trashtype / 3 != 0 && board[y][x].trashtype / 3 != 1) || (board[y - 1][x].trash && (multiplayertrashmetal ? board[y - 1][x].color == board[y][x].color : board[y - 1][x].color == 0)))) {
				count += spreadmatchtrash(x, y - 1);
			}
			return count;
		}
		return 0;
	}

	private void processtrashqueue(int color) {
		if (!trashinput.isEmpty()) {
			//iterate through trash and process it
		}
	}

	private void animatematch() {
		int y2;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (board[y][x].matchanimationframe >= 0) {
					raise = false;
					board[y][x].matchanimationframe--;
					if (board[y][x].trash && board[y][x].dissappear && board[y][x].matchanimationframe * 2 - board[y][x].matchid * TILESIZE == 0) {
						int frame = board[y][x].matchanimationframe;
						board[y][x] = new Block(r.nextInt(colorcount) + 1, false, 0);
						board[y][x].dissappear = false;
						board[y][x].matchanimationframe = frame;
						board[y][x].chainpowered = true;
					}
					if (board[y][x].matchanimationframe == 0) {
						if (board[y][x].trash) {
							if (board[y][x].trashtype / 3 == 2 && !board[y + 1][x].trash) {
								board[y][x].trashtype += 3;
							}
							if (board[y][x].trashtype / 3 == 1 && !board[y + 1][x].trash) {
								board[y][x].trashtype -= 3;
							}
						} else {
							if (board[y][x].dissappear)
								board[y][x] = new Block();
						}
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
				if ((board[y][x].trash || board[y][x].color != 0) && !board[y][x].inair && !board[y][x].inAnimation() && (y == HEIGHT - 1 || !board[y + 1][x].inSwapAnimation()) || board[y][x].removechainpower) {
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
				if (mychain == 2) {
					trashoutput.add(new Trash(0, WIDTH, player));
				} else {
					trashoutput.add(new Trash(3, WIDTH, player));
					for (int i = 2; i < mychain - 1; i++) {
						trashoutput.add(new Trash(2, WIDTH, player));
					}
					trashoutput.add(new Trash(1, WIDTH, player));
				}
				trashoutput.addAll(combotrashoutput);
				combotrashoutput.clear();
			}
			mychain = 1;

		}
		if ((!combotrashontop || raise)) {
			trashoutput.addAll(combotrashoutput);
			combotrashoutput.clear();
		}
	}

	private void clearline() {
		//coming soon
	}

	private void trash() {
		if (!trashoutput.isEmpty()) {
			trashinput.add(trashoutput.poll());
		}
		if (!trashrowvalid && !touchingTop() && !trashinput.isEmpty()) {
			Trash t = trashinput.poll();
			if (t.type == 0 || t.type == 3) {
				offset = r.nextInt(WIDTH - t.width + 1);
			}
			for (int x = 0; x < WIDTH; x++) {
				trashrow[x] = new Block();
			}//The zero is abritrary
			if (t.type == 1 && !board[1][0].trash) {
				t.type = 0;
			}
			if (t.type == 2 && !board[1][0].trash) {
				t.type = 3;
			}
			trashrow[offset] = new Block(t.color, true, t.type * 3);
			for (int x = offset + 1; x < offset + t.width - 1; x++) {
				trashrow[x] = new Block(t.color, true, t.type * 3 + 1);

			}

			trashrow[offset + t.width - 1] = new Block(t.color, true, t.type * 3 + 2);
			for (int x = 0; x < WIDTH; x++) {
				if (trashrow[x].isTrash() && (t.type == 1 || t.type == 2)) {
					trashrow[x].veldown = fallspeeddivisor;

				}
			}
			raise = false;
			trashrowvalid = true;
		}

	}

	private int offset = 0;

	private void lift(GameInput input) {
		if (forcelift || input.raisingStack && (explodelift || raise) && !touchingTop()) {
			stopframes /= 2;
			raiseprogress++;
			forcelift = true;
		} else if (raise && liftmultiplier > 0 && stoptimermultiplier != -1) {
			if (stopframes > 0) {
				if (stopframes > stoptimemax) {
					stopframes = stoptimemax;
				}
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
		collapse(map, nextrow);
	}

	private static void collapse(boolean[][] map, Block[] row) {
		boolean changehappened = false;
		int collapsed = 0;
		do {
			do {
				changehappened = false;
				//collapse anything collapsable
				for (int x = 0; x < WIDTH; x++) {
					if (row[x].color == 0) {
						int val = indexofonlyfalse(map[x]);
						if (val != -1) {
							row[x].color = val + 1;
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
						if (row[x2].color != 0) {
							if (color == 0)
								color = row[x2].color;
							else if (color != row[x2].color)
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
				if (row[x].color == 0) {
					if (nextcollapse == 0) {
						int val = r.nextInt(falsecount(map[x]));
						for (int y = 0; y < map[0].length; y++) {
							if (!map[x][y]) {
								if (val == 0) {
									row[x].color = y + 1;
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

	private static int falsecount(boolean[] b) {
		int count = 0;
		for (int i = 0; i < b.length; i++) {
			if (!b[i])
				count++;
		}
		return count;
	}

	private static int indexofonlyfalse(boolean[] b) {

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

	private boolean coordshifted = false;

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
		if (trashrowvalid) {
			return 9;
		}
		return trashinput.size() * 48 / maxtrashheight;
	}

	public int getStopTime() {
		return stopframes * 48 / stoptimemax;
	}

	public int getFallSpeedDivisor() {
		return fallspeeddivisor;
	}

	public boolean isGameOver() {
		return gameover;
	}

}
