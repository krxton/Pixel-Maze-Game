package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class TreeItem extends Item {
    public TreeItem(int x, int y, BufferedImage s){ super(x,y,s,"Tree"); }
    @Override public void onPickup(Player p, World w){ p.trees++; }
    @Override public Item copyAt(int nx, int ny){ return new TreeItem(nx,ny,sprite); }
}
