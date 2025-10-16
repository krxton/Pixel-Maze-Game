package game.items;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class SnowflakeItem extends Item {
    public SnowflakeItem(int x, int y, BufferedImage s){ super(x,y,s,"Snowflake"); }
    @Override public void onPickup(Player p, World w){ p.snowflakes++; }
    @Override public Item copyAt(int nx, int ny){ return new SnowflakeItem(nx,ny,sprite); }
}
