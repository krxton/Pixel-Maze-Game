package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class GiftItem extends Item {
    public GiftItem(int x, int y, BufferedImage s){ super(x,y,s,"Gift"); }
    @Override public void onPickup(Player p, World w){ p.gifts++; }
    @Override public Item copyAt(int nx, int ny){ return new GiftItem(nx,ny,sprite); }
}
