package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class RainbowItem extends Item {
    public RainbowItem(int x, int y, BufferedImage s){ super(x,y,s,"Rainbow"); }
    @Override public void onPickup(Player p, World w){ p.rainbows++; }
    @Override public Item copyAt(int nx, int ny){ return new RainbowItem(nx,ny,sprite); }
}
