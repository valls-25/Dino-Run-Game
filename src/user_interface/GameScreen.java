package user_interface;

import static user_interface.GameWindow.SCREEN_HEIGHT;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import game_object.Clouds;
import game_object.Dino;
import game_object.Land;
import game_object.Score;
import manager.CoinManager;
import manager.ControlsManager;
import manager.EnemyManager;
import manager.SoundManager;
import misc.Controls;
import misc.DinoState;
import misc.GameState;
import game_object.Shield;
import game_object.Block;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings(value = { "serial" })
public class GameScreen extends JPanel implements Runnable {

	// Game loop thread
	private Thread thread;
	private Shield shield;

	// Game speed configuration
	private static final int STARTING_SPEED_X = -5;
	private static final double DIFFICULTY_INC = -0.0002;

	// Physics constants
	public static final double GRAVITY = 0.4;
	public static final int GROUND_Y = 380;
	public static final double SPEED_Y = -12;

	// Frame rate config
	private final int FPS = 100;
	private final int NS_PER_FRAME = 1_000_000_000 / FPS;

	// Game variables
	private double speedX = STARTING_SPEED_X;
	private GameState gameState = GameState.GAME_STATE_START;
	private int introCountdown = 1000;
	private boolean introJump = true;
	private boolean showHitboxes = false;
	private boolean collisions = true;

	// Game components
	private Controls controls;
	private Score score;
	private Dino dino;
	private Land land;
	private Clouds clouds;
	private EnemyManager eManager;
	private SoundManager gameOverSound;
	private ControlsManager cManager;
	private int shakeDuration = 0;
	private int shakeIntensity = 20; // pixels to shake

	// Shield mechanics
	private static final int SHIELD_SPAWN_INTERVAL = 10000; // 20 seconds in milliseconds
	private long lastShieldSpawnTime;
	private BufferedImage shieldImg;
	private ArrayList<Block> shieldPowerUps;

	private CoinManager coinManager;
	private long dayDuration = 30000; // Duration of day in milliseconds
	private long nightDuration = 30000; // Duration of night in milliseconds
	private long currentTime = 0; // Current time in the game
	private boolean isDay = true; // Flag to check if it's day or night
	private long lastFrameTime; // Variable to store the last frame time
	private BufferedImage sunImg;
	private BufferedImage moonImg;

	// Constructor: initialize game objects and input
	public GameScreen() {
		thread = new Thread(this);
		controls = new Controls(this);
		super.add(controls.pressUp);
		super.add(controls.releaseUp);
		super.add(controls.pressDown);
		super.add(controls.releaseDown);
		super.add(controls.pressDebug);
		super.add(controls.pressPause);
		cManager = new ControlsManager(controls, this);
		score = new Score(this);
		dino = new Dino(controls);
		land = new Land(this);
		clouds = new Clouds(this);
		eManager = new EnemyManager(this);
		gameOverSound = new SoundManager("resources/dead.wav");
		gameOverSound.startThread();

		// Initialize shield-related objects
		shield = new Shield(5); // Shield lasts for 5 seconds
		shieldPowerUps = new ArrayList<>();
		shieldImg = getImage("resources/shield.png");
		sunImg = getImage("resources/sun.png"); // Load sun image
		moonImg = getImage("resources/moon.png"); // Load moon image

		lastShieldSpawnTime = System.currentTimeMillis();
		coinManager = new CoinManager(this);
		lastFrameTime = System.currentTimeMillis();
		startGame(); // Initialize game state properly
	}

	// Start game loop thread
	public void startThread() {
		thread.start();
	}

	// Reset state for new game
	public void startGame() {
		if (shield != null) {
			shield.reset(); // Reset the shield timer
			shield.deactivate(); // Ensure shield starts deactivated
		}
		shieldPowerUps.clear();
		lastShieldSpawnTime = System.currentTimeMillis();
	}

	// Game loop
	@Override
	public void run() {
		long prevFrameTime = System.nanoTime();
		int waitingTime = 0;
		while (true) {
			updateTime();
			cManager.update();
			updateFrame(); // Game logic
			repaint(); // Render
			lastFrameTime = System.currentTimeMillis();
			waitingTime = (int) ((NS_PER_FRAME - (System.nanoTime() - prevFrameTime)) / 1_000_000);
			if (waitingTime < 0)
				waitingTime = 1;
			SoundManager.WAITING_TIME = waitingTime;

			// Add delay after game over
			if (gameState == GameState.GAME_STATE_OVER)
				waitingTime = 1000;
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			prevFrameTime = System.nanoTime();
		}
	}

	// Get current speed
	public double getSpeedX() {
		return speedX;
	}

	// Get current game state
	public GameState getGameState() {
		return gameState;
	}

	// Get dino reference
	public Dino getDino() {
		return dino;
	}

	int sunX = SCREEN_WIDTH - 200;
	int sunY = 50;
	int moonX = SCREEN_WIDTH - 200;
	int moonY = 50;
	// Render the screen

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (isDay) {
			g.setColor(new Color(246, 246, 246)); // Sky blue for day
		} else {
			g.setColor(new Color(0, 1, 3)); // Dark blue for night
		}

		g.fillRect(0, 0, getWidth(), getHeight()); // Fill background FIRST

		// Now draw the sun or moon
		if (isDay) {
			g.drawImage(sunImg, sunX, sunY, 100, 100, null); // Draw sun
		} else {
			g.drawImage(moonImg, moonX, moonY, 100, 100, null); // Draw moon
		}

		// // Screen shake effect
		int offsetX = 0;
		int offsetY = 0;

		// // Apply shake if needed
		if (gameState == GameState.GAME_STATE_IN_PROGRESS && shakeDuration > 0) {
			offsetX = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
			offsetY = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
			shakeDuration--;
		}

		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(offsetX, offsetY);

		// Draw background
		// g2.setColor(new Color(246, 246, 246));
		// g2.fillRect(0, 0, getWidth(), getHeight());

		// Render based on game state
		switch (gameState) {
			case GameState.GAME_STATE_START:
				startScreen(g2);
				break;
			case GameState.GAME_STATE_INTRO:
				introScreen(g2);
				break;
			case GameState.GAME_STATE_IN_PROGRESS:
				inProgressScreen(g2);
				break;
			case GameState.GAME_STATE_OVER:
				gameOverScreen(g2);
				break;
			case GameState.GAME_STATE_PAUSED:
				pausedScreen(g2);
				break;
			default:
				break;
		}
		g2.dispose();
	}

	// // Start screen shake effect
	public void triggerShake() {
		shakeDuration = 50;
	}

	// Update logic for all frames
	private void updateFrame() {
		switch (gameState) {
			case GAME_STATE_INTRO:
				dino.updatePosition();
				if (!introJump && dino.getDinoState() == DinoState.DINO_RUN)
					land.updatePosition();
				clouds.updatePosition();
				introCountdown += speedX;
				if (introCountdown <= 0)
					gameState = GameState.GAME_STATE_IN_PROGRESS;
				if (introJump) {
					dino.jump();
					dino.setDinoState(DinoState.DINO_JUMP);
					introJump = false;
				}
				break;
			case GAME_STATE_IN_PROGRESS:
				// System.out.println("Coins: " + coinManager.getCoinCount() + ", Active coins:
				// " + coinManager.getActiveCoins());

				speedX += DIFFICULTY_INC;
				dino.updatePosition();
				land.updatePosition();
				clouds.updatePosition();
				eManager.updatePosition();
				shield.update();
				coinManager.update();

				// Handle shield power-ups
				for (Iterator<Block> iterator = shieldPowerUps.iterator(); iterator.hasNext();) {
					Block shieldPU = iterator.next();
					shieldPU.x += speedX;

					// Collision detection
					if (dino.getHitbox()
							.intersects(new Rectangle((int) shieldPU.x, shieldPU.y, shieldPU.width, shieldPU.height))) {
						shield.activate();
						iterator.remove();
					} else if (shieldPU.x + shieldPU.width < 0) {
						iterator.remove();
					}
				}

				// Spawn shield if enough time passed
				if (System.currentTimeMillis() - lastShieldSpawnTime >= SHIELD_SPAWN_INTERVAL) {
					spawnShieldPowerUp();
					lastShieldSpawnTime = System.currentTimeMillis();
				}

				// Enemy collision handling
				if (eManager.isCollision(dino.getHitbox())) {
					if (shield.isActive()) {
						System.out.println("Shield protected from collision!");
					} else {
						gameState = GameState.GAME_STATE_OVER;
						dino.dinoGameOver();
						score.writeScore();
						gameOverSound.play();
						System.out.println("Collision detected, triggering screen shake.");
						triggerShake();
					}
				}

				score.scoreUp();
				break;
			default:
				break;
		}
	}

	// Draw debug lines and hitboxes
	private void drawDebugMenu(Graphics g) {
		g.setColor(Color.RED);
		g.drawLine(0, GROUND_Y, getWidth(), GROUND_Y);
		dino.drawHitbox(g);
		eManager.drawHitbox(g);
		coinManager.drawHitbox(g);
		String speedInfo = "SPEED_X: " + String.valueOf(Math.round(speedX * 1000D) / 1000D);
		g.drawString(speedInfo, (int) (SCREEN_WIDTH / 100), (int) (SCREEN_HEIGHT / 25));
	}

	// Start screen rendering
	private void startScreen(Graphics g) {
		land.draw(g);
		dino.draw(g);
		BufferedImage introImage = getImage("resources/intro-text.png");
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, introCountdown / 1000f));
		g2d.drawImage(introImage, SCREEN_WIDTH / 2 - introImage.getWidth() / 2, SCREEN_HEIGHT / 2 - introImage.getHeight(),
				null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	// Intro screen rendering
	private void introScreen(Graphics g) {
		clouds.draw(g);
		startScreen(g);
	}

	// Main game rendering during active play
	private void inProgressScreen(Graphics g) {
		clouds.draw(g);
		land.draw(g);
		eManager.draw(g);

		// Draw power-ups
		for (Block shieldPU : shieldPowerUps) {
			g.drawImage(shieldPU.img, (int) shieldPU.x, shieldPU.y, shieldPU.width, shieldPU.height, null);
		}

		// // Shield effect
		// if (shield.isActive()) {
		// Graphics2D g2d = (Graphics2D) g;
		// g2d.setColor(new Color(0, 150, 255, 80));
		// Rectangle dinoBox = dino.getHitbox();
		// g2d.fillOval(dinoBox.x - 5, dinoBox.y - 5, dinoBox.width + 20, dinoBox.height
		// + 20);
		// }

		if (shield.isActive()) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(new Color(0, 150, 255, 80));

			Rectangle dinoBox = dino.getHitbox();

			// Calculate center of dino
			int centerX = dinoBox.x + dinoBox.width / 2;
			int centerY = dinoBox.y + dinoBox.height / 2;

			// Set oval size
			int ovalWidth = dinoBox.width + 60;
			int ovalHeight = dinoBox.height + 65;

			// Top-left corner of the oval to center it
			int ovalX = centerX - ovalWidth / 2;
			int ovalY = centerY - ovalHeight / 2;

			g2d.fillOval(ovalX, ovalY, ovalWidth, ovalHeight);
		}

		coinManager.draw(g);
		dino.draw(g);
		score.draw(g);

		// Shield timer
		if (shield.isActive()) {
			g.setColor(new Color(0, 150, 255));
			g.setFont(new Font("Arial", Font.BOLD, 14));
			g.drawString("Shield: " + shield.getRemainingTime() + "s", SCREEN_WIDTH - 150, 40);
		}

		// Time till next shield
		long timeUntilNextShield = Math.max(0, SHIELD_SPAWN_INTERVAL - (System.currentTimeMillis() - lastShieldSpawnTime));
		g.setColor(new Color(60, 179, 113));
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.drawString("Next Shield: " + (timeUntilNextShield / 1000) + "s", SCREEN_WIDTH - 150, 60);

		if (showHitboxes)
			drawDebugMenu(g);
		g.setColor(Color.BLACK);
		g.drawString("Coins: " + coinManager.getCoinCount(), 50, 50);
	}

	// Game over rendering
	private void gameOverScreen(Graphics g) {
		inProgressScreen(g);
		BufferedImage gameOverImage = getImage("resources/game-over.png");
		BufferedImage replayImage = getImage("resources/replay.png");
		g.drawImage(gameOverImage, SCREEN_WIDTH / 2 - gameOverImage.getWidth() / 2,
				SCREEN_HEIGHT / 2 - gameOverImage.getHeight() * 2, null);
		g.drawImage(replayImage, SCREEN_WIDTH / 2 - replayImage.getWidth() / 2, SCREEN_HEIGHT / 2, null);
	}

	// Paused screen rendering
	private void pausedScreen(Graphics g) {
		inProgressScreen(g);
		BufferedImage pausedImage = getImage("resources/paused.png");
		g.drawImage(pausedImage, SCREEN_WIDTH / 2 - pausedImage.getWidth() / 2, SCREEN_HEIGHT / 2 - pausedImage.getHeight(),
				null);
	}

	// Input action: jump
	public void pressUpAction() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS) {
			dino.jump();
			dino.setDinoState(DinoState.DINO_JUMP);
		}
	}

	// Input action: start game or restart
	public void releaseUpAction() {
		if (gameState == GameState.GAME_STATE_START)
			gameState = GameState.GAME_STATE_INTRO;
		if (gameState == GameState.GAME_STATE_OVER) {
			speedX = STARTING_SPEED_X;
			score.scoreReset();
			eManager.clearEnemy();
			dino.resetDino();
			clouds.clearClouds();
			land.resetLand();
			coinManager.resetCoins();
			resetGame();
			gameState = GameState.GAME_STATE_IN_PROGRESS;
		}
	}

	// Input action: duck
	public void pressDownAction() {
		if (dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
			dino.setDinoState(DinoState.DINO_DOWN_RUN);
	}

	// Input action: stand up from duck
	public void releaseDownAction() {
		if (dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
			dino.setDinoState(DinoState.DINO_RUN);
	}

	// Input action: toggle debug info
	public void pressDebugAction() {
		if (showHitboxes == false)
			showHitboxes = true;
		else
			showHitboxes = false;
		if (collisions == true)
			collisions = false;
		else
			collisions = true;
	}

	// Input action: pause/resume game
	public void pressPauseAction() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS)
			gameState = GameState.GAME_STATE_PAUSED;
		else
			gameState = GameState.GAME_STATE_IN_PROGRESS;
	}

	// Create a new shield pickup
	private void spawnShieldPowerUp() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS) {
			int xPosition = SCREEN_WIDTH + 50;
			int shieldHeight = 40;
			int yPosition = GROUND_Y - shieldHeight;
			shieldPowerUps.add(new Block(xPosition, yPosition, 40, shieldHeight, shieldImg));
		}
	}

	private void updateTime() {
		currentTime += System.currentTimeMillis() - lastFrameTime; // Update current time
		if (isDay && currentTime >= dayDuration) {
			isDay = false; // Switch to night
			currentTime = 0; // Reset current time
			System.out.println("Switched to Night");
		} else if (!isDay && currentTime >= nightDuration) {
			isDay = true; // Switch to day
			currentTime = 0; // Reset current time
			System.out.println("Switched to Day");
		}

	}

	// Reset shield and shake state
	public void resetGame() {

		shield.deactivate();
		shield.reset();
		shieldPowerUps.clear();
		lastShieldSpawnTime = System.currentTimeMillis();

		// shakeDuration = 0;
	}
}
