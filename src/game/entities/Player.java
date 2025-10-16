package game.entities;

import java.awt.image.BufferedImage;

public class Player extends Entity {
    public int shields=0, potions=0, snowflakes=0, gifts=0;
    public int rainbows=0, trees=0;  // 🌈 และ 🌳
    public int hp = 3;           // พลังชีวิต
    public int score = 0;        // คะแนน
    public int timeLeft = 120;   // เวลาต่อด่าน (วินาที)
    public Player(int x,int y, BufferedImage sprite) { super(x,y,sprite); }
}
