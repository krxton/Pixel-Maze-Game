package game.level;

public class Level {
    public final int rows, cols;
    public final int timeLimitSec;
    public final int startHP, startScore;

    // items
    public final int numSnowflake, numPotion, numShield, numGift;
    public final int numRainbow, numTree; // 🌈 🌳 ใหม่

    // enemies
    public final int enemyRed, enemyBlue, enemyYellow;

    // obstacles
    public final int obstTimeTrap, obstSpike, obstCurse;

    public Level(int rows, int cols, int timeLimitSec, int startHP, int startScore,
                 int numSnowflake, int numPotion, int numShield, int numGift,
                 int numRainbow, int numTree,
                 int enemyRed, int enemyBlue, int enemyYellow,
                 int obstTimeTrap, int obstSpike, int obstCurse){
        this.rows=rows; this.cols=cols; this.timeLimitSec=timeLimitSec;
        this.startHP=startHP; this.startScore=startScore;
        this.numSnowflake=numSnowflake; this.numPotion=numPotion; this.numShield=numShield; this.numGift=numGift;
        this.numRainbow=numRainbow; this.numTree=numTree;
        this.enemyRed=enemyRed; this.enemyBlue=enemyBlue; this.enemyYellow=enemyYellow;
        this.obstTimeTrap=obstTimeTrap; this.obstSpike=obstSpike; this.obstCurse=obstCurse;
    }
}
