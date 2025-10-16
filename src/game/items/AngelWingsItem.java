package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class AngelWingsItem extends Item {
    public AngelWingsItem(int x, int y, BufferedImage s){ super(x,y,s,"AngelWings"); }
    @Override public void onPickup(Player p, World w){ p.shields++; }
    @Override public Item copyAt(int nx, int ny){ return new AngelWingsItem(nx,ny,sprite); }
}
