package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class PotionItem extends Item {
    public PotionItem(int x, int y, BufferedImage s){ super(x,y,s,"Potion"); }
    @Override public void onPickup(Player p, World w){ p.potions++; }
    @Override public Item copyAt(int nx, int ny){ return new PotionItem(nx,ny,sprite); }
}
