package game;

public class Block {
	int color = 0;
	int veldown = 0;
	boolean trash = false;
	int matchclear = 0;
	boolean inair = false;
	boolean chainpowered = false;
	byte swapAnim = 0;
	byte offset = 0;
	byte animation = -1;

	public boolean canSwap() {
		return (!trash && animation < 0 && swapAnim == 0);
	}

	public boolean solid() {
		return !(color == 0 && canSwap());
	}

	public int getColor() {
		return color;
	}

	public boolean isTrash() {
		return trash;
	}

	public int getMatchclear() {
		return matchclear;
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

	public byte getAnimation() {
		return animation;
	}

}
