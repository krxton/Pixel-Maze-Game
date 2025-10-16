package game.entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {
    public int x,y;
    public BufferedImage sprite;
    public Entity(int x,int y, BufferedImage sprite){
        this.x=x; this.y=y; this.sprite=sprite;
    }
    public void draw(Graphics2D g, int TILE){
        if (sprite!=null) g.drawImage(sprite, x*TILE, y*TILE, TILE, TILE, null);
    }
}
