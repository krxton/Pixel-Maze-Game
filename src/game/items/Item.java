package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Item {
    public int x,y;
    protected BufferedImage sprite;
    protected String name;

    public Item(int x,int y, BufferedImage sprite, String name){
        this.x=x; this.y=y; this.sprite=sprite; this.name=name;
    }
    public void draw(Graphics2D g, int TILE){
        if (sprite!=null) g.drawImage(sprite, x*TILE, y*TILE, TILE, TILE, null);
    }
    public String getName(){ return name; }

    public abstract void onPickup(Player p, World w);
    public abstract Item copyAt(int nx, int ny);
}
