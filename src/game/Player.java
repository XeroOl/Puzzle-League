package game;

import java.util.Random;

import display.GameInput;

/**
 * This is area where the blocks and stuff are!
 * @author Edison
 */
public class Player {

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
	private Random r;
	private Block[][] board = new Block[12][6];
	private int cx = 2, cy = 6;
	public int mychain = 0;
	public boolean raise = true; // set to false if anything at all should stop the stack from raising

	public class Builder {
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

	}

	public void update(final GameInput input) {
		raise = true;
		cursor(input);
		swap(input);
		animatematch(); // set above block's chain to true
		fall();
		animateswap();
		match();
		resetchainflag(); // set ground block's chain to false, if there is no match nor block that is chain, tell trash() to send the trash;
		clearline();
		trash(); //sends trash, and adds sent trash
		lift(input);
	}

	public void cursor(GameInput input) {
		if (input.left && cx > 0)
			cx--;
		if (input.right && cx < board.length - 2)
			cx++;
		if (input.down && cy > 0)
			cy--;
		if (input.up && cy < board[0].length - 1)
			cy++;
	}

	public void swap(GameInput input) {
		if (input.swapping) {
			if (board[cy][cx].canSwap() && board[cy][cx + 1].canSwap()) {
				board[cy][cx].swapAnim = 4;
				board[cy][cx + 1].swapAnim = -4;
			}
		}
	}

	public void animatematch() {
		// TODO Auto-generated method stub

	}

	public void fall() {

	}

	public void animateswap() {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[0].length; y++) {

			}
		}

	}

	public void match() {

	}

	public void resetchainflag() {

	}

	public void clearline() {

	}

	public void trash() {

	}

	public void lift(GameInput input) {

	}

}
