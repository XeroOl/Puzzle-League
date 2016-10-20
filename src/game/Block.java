package game;

public class Block {
	int color = 0;
	int veldown = 0;
	boolean trash = false;
	boolean inair = false;
	boolean chainpowered = false;
	int chainnum = 0;
	byte swapAnim = 0;
	byte offset = 0;
	int matchanimationframe = -1;
	int matchid = -1;

	public boolean canSwap() {
		return (!trash && !inAnimation());
	}

	public boolean inAnimation() {
		return !(matchanimationframe < 0 && swapAnim == 0);
	}

	public boolean inMatchAnimation() {
		return matchanimationframe > 0;
	}

	public boolean isSolid() {
		return !(color == 0 && !inAnimation());
	}

	public int getColor() {
		return color;
	}

	public boolean isTrash() {
		return trash;
	}

	public boolean isInair() {
		return inair;
	}

	public boolean isChainpowered() {
		return chainpowered;
	}

	public byte getSwapAnim() {
		return swapAnim;
	}

	public byte getOffset() {
		return offset;
	}

	public int getMatchAnimationFrame() {
		return matchanimationframe * 2 / Player.TILESIZE - matchid == 0 ? matchid * Player.TILESIZE / 2 - matchanimationframe : matchanimationframe * 2 / Player.TILESIZE - matchid > 0 ? Player.TILESIZE/2 - 1 : 0;
	}

	public boolean canMatch() {
		return !trash && color != 0 && !inair && !inAnimation();
	}

	public boolean equals(Object o) {
		return o != null && o instanceof Block && ((Block) o).color == color;
	}

	public int getChainNum() {
		return chainnum;
	}
}
