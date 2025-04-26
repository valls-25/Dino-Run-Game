package manager;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import game_object.Coin;
import user_interface.GameScreen;

public class CoinManager {
  private GameScreen gameScreen;
  private List<Coin> coins;
  private Random random;
  private int coinCount;
  private long lastCoinTime;
  private static final int MIN_COIN_INTERVAL = 2000; // Minimum time between coin spawns in milliseconds
  private static final int MAX_ADDITIONAL_INTERVAL = 3000; // Additional random time to add
  private static final int MAX_COINS = 5; // Limit maximum coins on screen
  private static final int COIN_POOL_SIZE = 10; // Size of the coin pool

  private List<Coin> coinPool; // Object pool for coins

  public CoinManager(GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    coins = new ArrayList<>();
    coinPool = new ArrayList<>(COIN_POOL_SIZE);
    random = new Random();
    coinCount = 0;
    lastCoinTime = System.currentTimeMillis();

    // Initialize the coin pool
    for (int i = 0; i < COIN_POOL_SIZE; i++) {
      coinPool.add(new Coin(gameScreen, gameScreen.getWidth()));
    }
  }

  public void update() {
    // Update coin positions and check for collisions
    for (Iterator<Coin> iterator = coins.iterator(); iterator.hasNext();) {
      Coin coin = iterator.next();
      coin.update();

      // Check collision with dino
      if (gameScreen.getDino().getHitbox().intersects(coin.getHitbox())) {
        coinCount++;
        iterator.remove();
        // Return the coin to the pool
        resetCoin(coin);
      } else if (coin.isOutOfScreen()) {
        iterator.remove();
        // Return the coin to the pool
        resetCoin(coin);
      }
    }

    // Only spawn new coins if we're under the limit
    if (coins.size() < MAX_COINS && shouldSpawnCoin()) {
      addCoin();
      lastCoinTime = System.currentTimeMillis();
    }
  }

  private boolean shouldSpawnCoin() {
    return System.currentTimeMillis() - lastCoinTime > MIN_COIN_INTERVAL + random.nextInt(MAX_ADDITIONAL_INTERVAL);
  }

  public void addCoin() {
    if (!coinPool.isEmpty()) {
      Coin coin = coinPool.remove(coinPool.size() - 1); // Get a coin from the pool
      coin.reset(); // Reset the coin's state
      coins.add(coin);
    }
  }

  private void resetCoin(Coin coin) {
    coin.deactivate(); // Deactivate the coin instead of removing it
    coinPool.add(coin); // Return the coin to the pool
  }

  public int getCoinCount() {
    return coinCount;
  }

  public int getActiveCoins() {
    return coins.size();
  }

  public void draw(Graphics g) {
    for (Coin coin : coins) {
      if (coin.isActive()) { // Check if the coin is active before drawing
        coin.draw(g);
      }
    }
  }

  public void drawHitbox(Graphics g) {
    for (Coin coin : coins) {
      coin.drawHitbox(g);
    }
  }

  public void resetCoins() {
    coins.clear(); // Clear active coins
    coinCount = 0; // Reset coin count
    lastCoinTime = System.currentTimeMillis(); // Reset last coin spawn time
    coinPool.clear(); // Clear the pool if needed
    // Reinitialize the pool
    for (int i = 0; i < COIN_POOL_SIZE; i++) {
      coinPool.add(new Coin(gameScreen, gameScreen.getWidth()));
    }
  }
}