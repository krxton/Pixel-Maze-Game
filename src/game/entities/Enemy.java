package game.entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Enemy extends Entity {
    public enum Color { RED, BLUE, YELLOW }

    public Color color;
    public int hp = 1;
    private boolean frozen=false;
    private long frozenUntil=0;
    public Point lureTarget=null;
    private long lureUntil=0;

    public Enemy(int x,int y, Color c, BufferedImage sprite){
        super(x,y,sprite);
        this.color=c;
        if (c==Color.RED) hp=2;
    }

    public boolean isFrozen(){ return frozen && System.currentTimeMillis()<frozenUntil; }
    public void freezeForMillis(long ms){ frozen=true; frozenUntil=System.currentTimeMillis()+ms; }

    public boolean isLured(){ return lureTarget!=null && System.currentTimeMillis()<lureUntil; }
    public void lureTo(Point p, long ms){ lureTarget=p; lureUntil=System.currentTimeMillis()+ms; }
}
