package game.obstacles;

import game.entities.Player;
import game.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Obstacle {
    public int x, y;
    protected BufferedImage sprite;
    public String name;

    public Obstacle(int x, int y, BufferedImage sprite, String name){
        this.x=x; this.y=y; this.sprite=sprite; this.name=name;
    }
    public void draw(Graphics2D g, int TILE){
        if (sprite != null) g.drawImage(sprite, x*TILE, y*TILE, TILE, TILE, null);
    }
    public abstract void onStep(Player p, World w);
}
