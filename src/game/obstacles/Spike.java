package game.obstacles;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class Spike extends Obstacle {
    public Spike(int x, int y, BufferedImage s){ super(x,y,s,"Spike"); }
    @Override public void onStep(Player p, World w){
        p.hp -= 1;
    }
}
