package game_object;

import static user_interface.GameScreen.GROUND_Y;
import static util.Resource.getImage;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

import user_interface.GameScreen;

public class Coin {
  private BufferedImage coinImage;
  private Rectangle hitbox;
  private int posX, posY;
  private int width = 30;
  private int height = 30;
  private GameScreen gameScreen;
  private static final Random random = new Random();
  private boolean isActive; // Indicates if the coin is active

  public Coin(GameScreen gameScreen, int posX) {
    this.gameScreen = gameScreen;
    this.posX = posX;
    this.isActive = false; // Initially inactive
    this.coinImage = getImage("resources/coin.png"); // Load image once
    reset(); // Initialize the coin's position
  }

  public void update() {
    if (isActive) {
      posX += gameScreen.getSpeedX();
      hitbox.x = posX;
    }
  }

  public void draw(Graphics g) {
    if (isActive) {
      g.drawImage(coinImage, posX, posY, width, height, null);
    }
  }

  public void drawHitbox(Graphics g) {
    if (isActive) {
      g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }
  }

  public Rectangle getHitbox() {
    return hitbox;
  }

  public boolean isOutOfScreen() {
    return (posX + width) < 0;
  }

  public void reset() {
    // Reset the coin's position and state
    this.posX = gameScreen.getWidth(); // Start from the right edge of the screen
    this.posY = GROUND_Y - height - 50 - random.nextInt(100); // Random height above ground
    this.isActive = true; // Mark the coin as active
    hitbox = new Rectangle(posX, posY, width, height); // Update hitbox position
  }

  public boolean isActive() {
    return isActive; // Return the active state of the coin
  }

  public void deactivate() {
    this.isActive = false; // Mark the coin as inactive when collected or out of screen
  }
}