package game.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SpriteLoader {
    public static BufferedImage load(String path){
        try { return ImageIO.read(new File(path)); }
        catch (Exception e){ System.err.println("Missing sprite: "+path); return null; }
    }
    public static BufferedImage scale(BufferedImage src, int w,int h){
        if (src==null) return null;
        BufferedImage out = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0,0,w,h,null);
        g.dispose();
        return out;
    }
}
