package game.obstacles;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class TimeTrap extends Obstacle {
    public TimeTrap(int x, int y, BufferedImage s){ super(x,y,s,"TimeTrap"); }
    @Override public void onStep(Player p, World w){
        p.timeLeft -= 5; if (p.timeLeft < 0) p.timeLeft = 0;
    }
}
