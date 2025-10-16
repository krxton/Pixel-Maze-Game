package game.obstacles;

import game.entities.Player;
import game.world.World;

import java.awt.image.BufferedImage;

public class CurseTile extends Obstacle {
    public CurseTile(int x, int y, BufferedImage s){ super(x, y, s, "Curse"); }

    @Override
    public void onStep(Player p, World w){
        p.score -= 10;  // ลดคะแนน 10 แต้มเมื่อเหยียบ
        // จะไม่หายไปจากแผนที่ เดินเหยียบซ้ำก็โดนซ้ำได้
    }
}
