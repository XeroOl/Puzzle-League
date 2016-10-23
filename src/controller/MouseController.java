package controller;

import game.GameField;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import display.Display;

public class MouseController extends Controller implements MouseMotionListener, MouseListener {
	int mousey = 0;
	boolean dragging = false;
	int mousex = 0;
	int delay = 0;
	boolean left = false;
	boolean swap;
	GameInput myInput;
	Display d;
	GameField gf;

	public MouseController(Display d, GameField gf) {
		this.d = d;
		this.gf = gf;
		d.addMouseListener(this);
		d.addMouseMotionListener(this);
		myInput = new GameInput();
		myInput.raisingStack = false;
		myInput.swapping = false;
		myInput.cx = 2;
		myInput.cy = 6;
	}

	@Override
	public GameInput getInput() {
		myInput.swapping = false;
		if (gf.adjustY() && myInput.cy != 0) {
			myInput.cy--;
		}
		if (dragging) {

			label: if (delay == 0) {
				if (left && mousex > myInput.cx) {
					left = false;
				} else if (!left && mousex <= myInput.cx) {
					left = true;
				} else if (mousex < myInput.cx) {
					myInput.cx--;
					left = true;
				} else if (mousex > myInput.cx + 1 && myInput.cx < GameField.WIDTH - 2) {
					myInput.cx++;
					left = false;
				} else {
					break label;
				}
				myInput.swapping = true;
				delay = 3;
			} else {

				delay--;
			}
		}
		return myInput;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mousex = verifyx((int) (e.getX() / Display.scale / GameField.TILESIZE) - Display.OFFSET_X);

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			dragging = true;
			int x = verifyx((int) (e.getX() / Display.scale / GameField.TILESIZE) - Display.OFFSET_X);
			int y = verifyy((int) ((e.getY() / Display.scale + gf.getRaiseProgress()) / GameField.TILESIZE) - Display.OFFSET_Y);
			mousex = x;
			left = myInput.cx >= x;
			myInput.cx = myInput.cx >= x ? x : x - 1;
			myInput.cy = y;
		} else {
			myInput.raisingStack = true;
		}
	}

	private int verifyx(int x) {
		return 0 > x ? 0 : x >= GameField.WIDTH ? GameField.WIDTH - 1 : x;
	}

	private int verifyy(int y) {
		return 0 > y ? 0 : y >= GameField.HEIGHT ? GameField.HEIGHT - 1 : y;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			dragging = false;
			delay = 0;
		} else {
			myInput.raisingStack = false;
		}
	}

	//****************************************************
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
