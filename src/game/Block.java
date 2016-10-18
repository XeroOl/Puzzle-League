package game;

public class Block {
	int color = 0;
	boolean trash;
	int matchclear;
	boolean inair;
	boolean chainpowered;
	byte swapAnim = 0;
	byte offset = 0;
	byte animation = -1;

	public boolean canSwap() {
		return (!trash && animation < 0 && swapAnim == 0);
	}
}
