package game_object;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Block {
    public double x;
    public int y;
    public int width;
    public int height;
    public BufferedImage img;

    public Block(double x, int y, int width, int height, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public void draw(Graphics g) {
        g.drawImage(img, (int)x, y, width, height, null);
    }
} 