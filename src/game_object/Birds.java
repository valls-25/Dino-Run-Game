package game_object;

import misc.Animation;
import user_interface.GameScreen;

import static user_interface.GameScreen.GROUND_Y;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import manager.EnemyManager;

public class Birds {

	private class Bird extends GameObj {

		private Animation birdFly;

		private Bird(double x, int y, Animation birdFly) {
			super(x, y);
			this.birdFly = birdFly;
		}

		@Override
		public void update(double speedX) {
			this.x += speedX;
			birdFly.updateSprite();
		}

		@Override
		public void draw(Graphics g) {
			g.drawImage(birdFly.getSprite(), (int) x,
					birdFly.getSprite().getHeight() < WINGS_DOWN_HEIGHT ? y + HITBOX_MODELS_DIFF_IN_Y : y, null);
		}

		@Override
		public Rectangle getHitbox() {
			return new Rectangle((int) x + HITBOX_WINGS_UP[0],
					birdFly.getSprite().getHeight() < WINGS_DOWN_HEIGHT ? y + HITBOX_WINGS_UP[1] : y + HITBOX_WINGS_DOWN[1],
					birdFly.getSprite().getWidth() + HITBOX_WINGS_UP[2],
					birdFly.getSprite().getHeight() < WINGS_DOWN_HEIGHT ? birdFly.getSprite().getHeight() + HITBOX_WINGS_UP[3]
							: birdFly.getSprite().getHeight() + HITBOX_WINGS_DOWN[3]);
		}

		public Animation getAnimation() {
			return birdFly;
		}
	}

	private static final int HITBOX_MODELS_DIFF_IN_Y = -12;
	private static final int[] HITBOX_WINGS_UP = { 20, 4, -40, -20 };
	private static final int[] HITBOX_WINGS_DOWN = { 20, 4, -40, -28 };
	private final int WINGS_DOWN_HEIGHT = getImage("resources/bird-fly-1.png").getHeight();

	private EnemyManager eManager;
	private GameScreen gameScreen;
	private List<Bird> birds;

	public Birds(GameScreen gameScreen, EnemyManager eManager) {
		this.eManager = eManager;
		this.gameScreen = gameScreen;
		birds = new ArrayList<Bird>();
	}

	public void updatePosition() {
		for (Iterator<Bird> i = birds.iterator(); i.hasNext();) {
			Bird bird = i.next();
			bird.update(gameScreen.getSpeedX() + gameScreen.getSpeedX() / 5);
		}
	}

	public boolean spaceAvailable() {
		for (Iterator<Bird> i = birds.iterator(); i.hasNext();) {
			Bird bird = i.next();
			if (SCREEN_WIDTH - (bird.x + bird.getAnimation().getSprite().getWidth()) < eManager.getDistanceBetweenEnemies()) {
				return false;
			}
		}
		return true;
	}

	public boolean createBird() {
		if (Math.random() * 100 < eManager.getBirdsPercentage()) {
			Animation birdFly = new Animation(400);
			birdFly.addSprite(getImage("resources/bird-fly-1.png"));
			birdFly.addSprite(getImage("resources/bird-fly-2.png"));
			birds.add(new Bird(SCREEN_WIDTH, (int) (Math.random() * (GROUND_Y - birdFly.getSprite().getHeight())), birdFly));
			return true;
		}
		return false;
	}

	public boolean isCollision(Rectangle dinoHitBox) {
		for (Iterator<Bird> i = birds.iterator(); i.hasNext();) {
			Bird bird = i.next();
			if (bird.getHitbox().intersects(dinoHitBox))
				return true;
		}
		return false;
	}

	public void clearBirds() {
		birds.clear();
	}

	public void draw(Graphics g) {
		for (Iterator<Bird> i = birds.iterator(); i.hasNext();) {
			Bird bird = i.next();
			bird.draw(g);
		}
	}

	public void drawHitbox(Graphics g) {
		g.setColor(Color.RED);
		for (Iterator<Bird> i = birds.iterator(); i.hasNext();) {
			Bird bird = i.next();
			Rectangle birdHitBox = bird.getHitbox();
			g.drawRect(birdHitBox.x, birdHitBox.y, (int) birdHitBox.getWidth(), (int) birdHitBox.getHeight());
		}
	}
}
