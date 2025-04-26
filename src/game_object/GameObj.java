package game_object;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObj {
  protected double x;
  protected int y;

  public GameObj(double x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void update(double speedX);

  public abstract void draw(Graphics g);

  public abstract Rectangle getHitbox();

  public double getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
