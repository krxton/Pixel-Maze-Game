package game.util;

import game.entities.Enemy;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GfxUtil {
    public static BufferedImage tint(BufferedImage src, Enemy.Color color){
        if (src==null) return null;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src,0,0,null);
        g.setComposite(AlphaComposite.SrcAtop);
        Color c = switch(color){
            case RED -> new Color(255,60,60,160);
            case BLUE -> new Color(80,120,255,160);
            case YELLOW -> new Color(255,210,70,160);
        };
        g.setColor(c);
        g.fillRect(0,0,out.getWidth(), out.getHeight());
        g.dispose();
        return out;
    }
}
