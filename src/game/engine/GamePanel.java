package game.engine;

import game.world.*;
import game.entities.*;
import game.items.*;
import game.obstacles.*;
import game.level.*;
import game.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    // --- ขนาด ---
    public static final int TILE  = 32;
    public static final int HUD_H = 92;

    // --- ระบบเกม ---
    private final javax.swing.Timer timer = new javax.swing.Timer(60, this);
    private final Random rng = new Random();
    private final JFrame host;

    // --- สไปรต์ ---
    private BufferedImage catImg, fireImg, snowImg, bottleImg, angelImg, giftImg, exitImg;
    private BufferedImage rainbowImg, treeImg;

    // --- สถานะเกม ---
    private World world;
    private int currentLevel = 1;
    private boolean gameOver = false, win = false;
    private String message = "";
    private boolean showHelp = false;

    // หน้าเริ่มเกม
    private boolean showStart = true;
    private JButton startBtn;

    // โหมดง่าย (Assist)
    private boolean assistMode = true; // << เปิดไว้ให้เล่นง่ายตั้งแต่แรก (กด F2 สลับได้)

    private long msAccumulator = 0;
    // กันอุปสรรคยิงซ้ำทุกเฟรม
    private int lastStepX = Integer.MIN_VALUE, lastStepY = Integer.MIN_VALUE;

    public GamePanel(JFrame host){
        this.host = host;
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);
        loadAssets();
        loadLevel(currentLevel);
        createOrPlaceStartButton();
        timer.start();
    }

    // โหลดภาพทั้งหมด
    private void loadAssets(){
        catImg    = SpriteLoader.load("assets/innocent.png");
        fireImg   = SpriteLoader.load("assets/angry.png");
        snowImg   = SpriteLoader.load("assets/snowflake.png");
        bottleImg = SpriteLoader.load("assets/bottle.png");
        angelImg  = SpriteLoader.load("assets/angel.png");
        giftImg   = SpriteLoader.load("assets/gift-box.png");
        exitImg   = SpriteLoader.load("assets/photo-book.png");
        rainbowImg= SpriteLoader.load("assets/rainbow.png");
        treeImg   = SpriteLoader.load("assets/tree.png");
    }

    // โหลดด่าน
    private void loadLevel(int lv){
        message = "";
        gameOver = false; win = false; showHelp = false; msAccumulator = 0;
        lastStepX = Integer.MIN_VALUE; lastStepY = Integer.MIN_VALUE;

        Level L = Levels.get(lv);
        if (L == null){ gameOver = true; win = true; message = "🎉 คุณเคลียร์ทุกด่านแล้ว!"; return; }

        world = new World(exitImg, L.rows, L.cols);

        Dimension pref = new Dimension(world.cols*TILE, world.rows*TILE + HUD_H);
        setPreferredSize(pref); setMinimumSize(pref); setSize(pref);
        if (host != null) host.pack();

        MazeGenerator.generate(world);

        world.player = new Player(1,1, SpriteLoader.scale(catImg, TILE, TILE));
        world.player.hp       = L.startHP;
        world.player.timeLeft = L.timeLimitSec;
        world.player.score    = L.startScore;

        // ---- Assist Mode: บัฟเริ่มเกม ----
        if (assistMode){
            world.player.hp = Math.min(5, world.player.hp + 2);     // +2 HP (เพดาน 5)
            world.player.timeLeft += 60;                             // +60 วินาที
            world.player.shields += 1;                               // เกราะฟรี 1
            world.player.snowflakes += 1;                            // ของหนีตาย 1
            world.player.rainbows   += 1;                            // วาร์ป 1
        }

        // ไอเทม
        placeItems(new SnowflakeItem(0,0, SpriteLoader.scale(snowImg, TILE, TILE)), L.numSnowflake);
        placeItems(new PotionItem(0,0,     SpriteLoader.scale(bottleImg, TILE, TILE)), L.numPotion);
        placeItems(new AngelWingsItem(0,0, SpriteLoader.scale(angelImg, TILE, TILE)), L.numShield);
        placeItems(new GiftItem(0,0,       SpriteLoader.scale(giftImg, TILE, TILE)), L.numGift);
        placeItems(new RainbowItem(0,0,    SpriteLoader.scale(rainbowImg, TILE, TILE)), L.numRainbow);
        placeItems(new TreeItem(0,0,       SpriteLoader.scale(treeImg,    TILE, TILE)), L.numTree);

        // ศัตรู (ลดจำนวนและความถึกเมื่อ Assist)
        addEnemies(Enemy.Color.RED,    L.enemyRed);
        addEnemies(Enemy.Color.BLUE,   L.enemyBlue);
        addEnemies(Enemy.Color.YELLOW, L.enemyYellow);

        // อุปสรรค (กันซ้อนกัน + ลดจำนวนเมื่อ Assist)
        int tTrap  = assistMode ? Math.max(0,(int)Math.floor(L.obstTimeTrap * 0.6)) : L.obstTimeTrap;
        int tSpike = assistMode ? Math.max(0,(int)Math.floor(L.obstSpike    * 0.6)) : L.obstSpike;
        int tCurse = assistMode ? Math.max(0,(int)Math.floor(L.obstCurse    * 0.6)) : L.obstCurse;

        for (int i=0;i<tTrap;i++){
            Point p; do { p = randomFloor(); } while (occupiedByObstacle(p.x,p.y));
            world.obstacles.add(new TimeTrap(p.x,p.y,null));
        }
        for (int i=0;i<tSpike;i++){
            Point p; do { p = randomFloor(); } while (occupiedByObstacle(p.x,p.y));
            world.obstacles.add(new Spike(p.x,p.y,null));
        }
        for (int i=0;i<tCurse;i++){
            Point p; do { p = randomFloor(); } while (occupiedByObstacle(p.x,p.y));
            world.obstacles.add(new CurseTile(p.x,p.y,null));
        }

        // ปรับตำแหน่งปุ่ม Start
        createOrPlaceStartButton();
    }

    // สร้าง/ย้ายปุ่ม Start ให้อยู่กลางจอ
    private void createOrPlaceStartButton(){
        int W = world.cols*TILE, H = world.rows*TILE;
        int bw = 220, bh = 52;
        int x = (W - bw)/2, y = (H - bh)/2 + 18;

        if (startBtn == null){
            startBtn = new JButton("Start Game");
            startBtn.setFocusPainted(false);
            startBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            startBtn.setBackground(new Color(255, 180, 60));
            startBtn.setForeground(Color.BLACK); // เปลี่ยนสีตัวอักษรได้ที่นี่
            startBtn.setOpaque(true);
            startBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120,80,20), 2),
                    BorderFactory.createEmptyBorder(6,16,6,16)
            ));
            startBtn.addActionListener(ev -> {
                showStart = false;
                startBtn.setVisible(false);
                requestFocusInWindow();
                message = "เริ่มเกม!";
            });
            add(startBtn);
        }
        startBtn.setBounds(x, y, bw, bh);
        startBtn.setVisible(showStart);
    }

    private void addEnemies(Enemy.Color color, int n){
        // ลดจำนวนศัตรูเมื่อ Assist
        int count = assistMode ? Math.max(0, (int)Math.floor(n * 0.6)) : n;
        for(int i=0;i<count;i++){
            Point p = randomFloorAwayFromPlayer(10);
            BufferedImage spr = GfxUtil.tint(SpriteLoader.scale(fireImg, TILE, TILE), color);
            Enemy e = new Enemy(p.x,p.y,color,spr);
            if (assistMode && color==Enemy.Color.RED) e.hp = 1; // RED ถึกน้อยลง
            world.enemies.add(e);
        }
    }

    private void placeItems(Item proto, int count){
        for(int i=0;i<count;i++){
            Point p = randomFloor();
            Item it = proto.copyAt(p.x, p.y);
            world.items.add(it);
        }
    }

    private Point randomFloor(){
        while(true){
            int x = rng.nextInt(world.cols), y = rng.nextInt(world.rows);
            if (world.passable(x,y) && !world.isExit(x,y)) return new Point(x,y);
        }
    }
    private Point randomFloorAwayFromPlayer(int minManhattan){
        while(true){
            Point p = randomFloor();
            if (Math.abs(p.x-world.player.x)+Math.abs(p.y-world.player.y)>=minManhattan) return p;
        }
    }

    // ตำแหน่งนี้มีอุปสรรคอยู่แล้วหรือไม่
    private boolean occupiedByObstacle(int x,int y){
        for (Obstacle ob : world.obstacles) if (ob.x==x && ob.y==y) return true;
        return false;
    }

    // ===== Game Loop =====
    @Override public void actionPerformed(ActionEvent e) {
        // ยังอยู่หน้า Start → พักเกม
        if (showStart){ repaint(); return; }

        if (!gameOver && !showHelp){
            moveEnemies();
            checkTileEffects();
            checkCollisions();
            tickTime();
        }
        repaint();
    }

    private void tickTime(){
        msAccumulator += 60;
        if (msAccumulator >= 1000){
            msAccumulator -= 1000;
            if (world.player.timeLeft > 0) world.player.timeLeft--;
            if (world.player.timeLeft <= 0){
                gameOver = true; win = false; message = "⏳ หมดเวลา – Game Over";
            }
        }
    }

    private void moveEnemies(){
        for (Enemy en : world.enemies){
            if (en.isFrozen()) continue;

            // ช้าลงใน Assist (RED เดิน 1/เฟรม แทน 2)
            int speed = (en.color==Enemy.Color.RED?2:1);
            if (assistMode && speed>1) speed = 1;

            for (int s=0;s<speed;s++){
                Point target = en.isLured()? en.lureTarget : new Point(world.player.x, world.player.y);
                int bestX=en.x, bestY=en.y, bestScore=manhattan(en.x,en.y,target.x,target.y);
                int[][] steps={{1,0},{-1,0},{0,1},{0,-1}};
                List<int[]> order = new ArrayList<>(java.util.Arrays.asList(steps));
                Collections.shuffle(order,rng);
                for (int[] d: order){
                    int nx=en.x+d[0], ny=en.y+d[1];
                    if (world.passable(nx,ny)){
                        int sc=manhattan(nx,ny,target.x,target.y);
                        if (sc<bestScore){ bestScore=sc; bestX=nx; bestY=ny; }
                    }
                }
                en.x=bestX; en.y=bestY;
            }
        }
    }
    private int manhattan(int x1,int y1,int x2,int y2){ return Math.abs(x1-x2)+Math.abs(y1-y2); }

    // เรียกเมื่อผู้เล่น "ก้าวเข้า" ช่องใหม่เท่านั้น
    private void checkTileEffects(){
        if (world.player.x == lastStepX && world.player.y == lastStepY) return;

        for (Obstacle ob : world.obstacles){
            if (ob.x==world.player.x && ob.y==world.player.y){

                // ปรับความแรงของอุปสรรคตาม Assist (คุมเอง แทนที่จะเรียก onStep เดิม)
                if (ob instanceof TimeTrap){
                    world.player.timeLeft -= assistMode ? 2 : 5;
                    if (world.player.timeLeft < 0) world.player.timeLeft = 0;
                    message = "⚠️ เหยียบกับดักเวลา -" + (assistMode?2:5) + " วินาที";
                } else if (ob instanceof Spike){
                    world.player.hp -= 1; // หนามยัง -1 เท่าเดิม เพื่อให้มีความท้าทาย
                    message = "⚠️ เหยียบหนาม -1 HP";
                } else if (ob instanceof CurseTile){
                    world.player.score -= assistMode ? 5 : 10;
                    message = "⚠️ คำสาป -" + (assistMode?5:10) + " คะแนน";
                }
            }
        }
        lastStepX = world.player.x; lastStepY = world.player.y;

        if (world.player.hp <= 0){ gameOver = true; win = false; message = "💥 พลังชีวิตหมด – Game Over"; }
    }

    private void checkCollisions(){
        // เก็บไอเทม
        Iterator<Item> it = world.items.iterator();
        while(it.hasNext()){
            Item item = it.next();
            if (item.x==world.player.x && item.y==world.player.y){
                item.onPickup(world.player, world);
                world.player.score += 5;
                message = pickupMessageFor(item);
                it.remove();
            }
        }
        // ชนศัตรู
        for (Iterator<Enemy> ei=world.enemies.iterator(); ei.hasNext(); ){
            Enemy en = ei.next();
            if (en.x==world.player.x && en.y==world.player.y){
                if (world.player.shields>0){
                    world.player.shields--; ei.remove(); world.player.score += 10;
                    message = "🛡️ เกราะช่วยชีวิต! ไฟตัวนั้นหายไป";
                }else{
                    world.player.hp--;
                    message = (world.player.hp<=0) ? "🔥 โดนไฟเผา – Game Over" : "เจ็บ! HP เหลือ "+world.player.hp;
                    if (world.player.hp<=0){ gameOver=true; win=false; }
                }
                break;
            }
        }
        // ชนะ
        if (!gameOver && world.isExit(world.player.x, world.player.y)){
            world.player.score += world.player.timeLeft;
            currentLevel++; loadLevel(currentLevel);
            message = "🎉 ผ่านด่าน! ไปด่าน " + currentLevel;
        }
    }

    // ข้อความเก็บไอเทมแบบเข้าใจง่าย
    private String pickupMessageFor(Item item){
        if (item instanceof SnowflakeItem)   return "❄️ เก็บ Snowflake x1 (กด Z เพื่อแช่แข็งศัตรู)";
        if (item instanceof PotionItem)      return "🧪 เก็บ Potion x1 (กด X โจมตีรอบตัว)";
        if (item instanceof AngelWingsItem)  return "👼 เกราะ +1 (กันตายอัตโนมัติ)";
        if (item instanceof GiftItem)        return "🎁 Gift x1 (กด C ล่อไฟเหลือง)";
        if (item instanceof RainbowItem)     return "🌈 Rainbow x1 (กด B วาร์ปเข้าใกล้ประตู)";
        if (item instanceof TreeItem)        return "🌳 Tree x1 (กด N ฟื้น +1HP และ +10s)";
        return "เก็บไอเทม";
    }

    // ===== Input =====
    @Override public void keyPressed(KeyEvent e) {
        // หน้า Start: Enter ก็เริ่ม
        if (showStart && e.getKeyCode()==KeyEvent.VK_ENTER){
            showStart = false;
            if (startBtn != null) startBtn.setVisible(false);
            message = "เริ่มเกม!";
            return;
        }

        // เปิด/ปิดวิธีเล่น
        if (e.getKeyCode()==KeyEvent.VK_H){ showHelp=!showHelp; message = showHelp? "📖 เปิดคู่มือวิธีเล่น":"ปิดคู่มือวิธีเล่น"; repaint(); return; }
        if (showHelp) return;

        // Toggle โหมดง่าย
        if (e.getKeyCode()==KeyEvent.VK_F2){
            assistMode = !assistMode;
            message = "โหมดง่าย: " + (assistMode? "เปิด ✅ (เริ่มด่านใหม่เพื่อให้มีผลเต็มๆ)":"ปิด ❌");
            return;
        }

        // จบเกมแล้วกด R = เริ่มใหม่และกลับหน้า Start
        if (gameOver && e.getKeyCode()==KeyEvent.VK_R){ currentLevel=1; loadLevel(currentLevel); showStart=true; createOrPlaceStartButton(); return; }

        Player p = world.player;
        int nx=p.x, ny=p.y;
        if (e.getKeyCode()==KeyEvent.VK_LEFT)  nx--;
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) nx++;
        if (e.getKeyCode()==KeyEvent.VK_UP)    ny--;
        if (e.getKeyCode()==KeyEvent.VK_DOWN)  ny++;
        if (world.passable(nx,ny)){ p.x=nx; p.y=ny; }

        if (e.getKeyCode()==KeyEvent.VK_Z) useSnowflake();
        if (e.getKeyCode()==KeyEvent.VK_X) usePotion();
        if (e.getKeyCode()==KeyEvent.VK_C) useGift();
        if (e.getKeyCode()==KeyEvent.VK_B) useRainbow();
        if (e.getKeyCode()==KeyEvent.VK_N) useTree();
        if (e.getKeyCode()==KeyEvent.VK_V) message = hudStatusText();
        if (e.getKeyCode()==KeyEvent.VK_R && !gameOver){ loadLevel(currentLevel); message = "เริ่มด่านนี้ใหม่"; }
    }

    private void useSnowflake(){
        if (world.player.snowflakes<=0){ message="ไม่มี ❄️ Snowflake"; return; }
        world.player.snowflakes--;
        int freezeMs = assistMode ? 6000 : 4000;
        for (Enemy en: world.enemies) en.freezeForMillis(freezeMs);
        message="❄️ แช่แข็งศัตรู " + (freezeMs/1000) + " วินาที";
    }
    private void usePotion(){
        if (world.player.potions<=0){ message="ไม่มี 🧪 Potion"; return; }
        world.player.potions--;
        int radius = assistMode ? 2 : 1; // โดนกว้างขึ้นเมื่อ Assist
        List<Enemy> rm = new ArrayList<>();
        for (Enemy en: world.enemies){
            if (Math.abs(en.x-world.player.x)<=radius && Math.abs(en.y-world.player.y)<=radius){
                int dmg = (assistMode && en.color==Enemy.Color.RED) ? 2 : 1;
                en.hp -= dmg;
                if (en.hp<=0){ rm.add(en); world.player.score += 10; }
            }
        }
        world.enemies.removeAll(rm);
        message="🧪 โจมตีรอบตัว" + (assistMode? " ระยะกว้าง":"");
    }
    private void useGift(){
        if (world.player.gifts<=0){ message="ไม่มี 🎁 Gift"; return; }
        world.player.gifts--;
        Point lure = new Point(world.player.x, world.player.y);
        int ms = assistMode ? 8000 : 5000;
        for (Enemy en: world.enemies)
            if (en.color== Enemy.Color.YELLOW) en.lureTo(lure, ms);
        message="🎁 วางของล่อ " + (ms/1000) + " วินาที (ไฟเหลืองจะเดินตาม)";
    }
    private void useRainbow(){
        if (world.player.rainbows<=0){ message="ไม่มี 🌈 Rainbow"; return; }
        world.player.rainbows--;
        Point exit = new Point(world.cols-2, world.rows-2);
        int steps = assistMode ? 8 : 6; // วาร์ปไกลขึ้น
        int x=world.player.x, y=world.player.y;
        for(int i=0;i<steps;i++){
            int bestX=x, bestY=y, best=manhattan(x,y,exit.x,exit.y);
            int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
            for(int[] d: dirs){
                int nx=x+d[0], ny=y+d[1];
                if (world.passable(nx,ny)){
                    int sc=manhattan(nx,ny,exit.x,exit.y);
                    if (sc<best){ best=sc; bestX=nx; bestY=ny; }
                }
            }
            if (bestX==x && bestY==y) break;
            x=bestX; y=bestY;
        }
        world.player.x=x; world.player.y=y;
        message="🌈 วาร์ปเข้าใกล้ทางออกแล้ว!";
    }
    private void useTree(){
        if (world.player.trees<=0){ message="ไม่มี 🌳 Tree"; return; }
        world.player.trees--;
        world.player.hp = Math.max(1, Math.min(world.player.hp+1, 5));
        world.player.timeLeft += assistMode ? 15 : 10; // ฟื้นเวลาเยอะขึ้นนิดหน่อย
        message="🌳 ฟื้นพลัง +1HP และ +" + (assistMode?15:10) + " วินาที";
    }

    private String hudStatusText(){
        return String.format(
            "สถานะ: ด่าน %d | HP %d | เวลา %ds | คะแนน %d | ❄️%d 🧪%d 🎁%d 👼%d 🌈%d 🌳%d",
            currentLevel, world.player.hp, world.player.timeLeft, world.player.score,
            world.player.snowflakes, world.player.potions, world.player.gifts, world.player.shields,
            world.player.rainbows, world.player.trees
        );
    }

    // ===== วาดภาพ =====
    @Override protected void paintComponent(Graphics g0){
        super.paintComponent(g0);
        Graphics2D g=(Graphics2D)g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        world.draw(g, TILE);

        int hudY = world.rows*TILE;
        drawClearHUD(g, hudY);

        if (showStart) drawStartOverlay(g);
        if (showHelp)  drawHelpOverlay(g);

        if (gameOver){
            g.setColor(new Color(0,0,0,180));
            g.fillRect(0,0,world.cols*TILE,world.rows*TILE);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 28f));
            String t = win? "ALL CLEAR!" : "GAME OVER";
            int tw = g.getFontMetrics().stringWidth(t);
            g.drawString(t, (world.cols*TILE - tw)/2, (world.rows*TILE)/2 - 20);
            g.setFont(getFont().deriveFont(16f));
            String tip="กด R เพื่อเริ่มใหม่";
            g.drawString(tip,(world.cols*TILE-g.getFontMetrics().stringWidth(tip))/2, (world.rows*TILE)/2+10);
        }
    }

    // HUD อ่านง่าย + แสดงสถานะโหมดง่าย
    private void drawClearHUD(Graphics2D g, int hudY){
        int W = world.cols * TILE;
        g.setColor(new Color(18,18,26)); g.fillRect(0, hudY, W, HUD_H);

        g.setColor(Color.WHITE); g.setFont(getFont().deriveFont(14f));
        String line1 = String.format(
            "ด่าน: %d   |   💖 HP: %d   |   ⏳ เวลา: %ds   |   ⭐ คะแนน: %d   |   โหมดง่าย: %s (F2)",
            currentLevel, world.player.hp, world.player.timeLeft, world.player.score,
            assistMode ? "เปิด" : "ปิด"
        );
        g.drawString(line1, 12, hudY + 24);

        String line2 = String.format(
            "ไอเทม →  ❄️Z:%d  🧪X:%d  🎁C:%d  👼:%d  🌈B:%d  🌳N:%d",
            world.player.snowflakes, world.player.potions, world.player.gifts, world.player.shields,
            world.player.rainbows, world.player.trees
        );
        g.drawString(line2, 12, hudY + 46);

        g.setColor(new Color(210,210,255));
        g.drawString(message, 12, hudY + 68);
    }

    // หน้าเริ่มเกม
    private void drawStartOverlay(Graphics2D g){
        int W = world.cols * TILE, H = world.rows * TILE;
        g.setColor(new Color(0,0,0,180)); g.fillRect(0, 0, W, H);

        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 32f));
        String title = "Cat in the Pixel Maze";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (W - tw)/2, 100);

        g.setFont(getFont().deriveFont(16f));
        int y = 140, lh = 26;
        String[] lines = {
            "🎯 เป้าหมาย: ไปยังประตูรูปสมุดหัวใจ (ขวาล่าง) ก่อนเวลาหมด",
            "🧭 บังคับ: ลูกศร ← ↑ → ↓  |  ❄️Z  🧪X  🎁C  🌈B  🌳N  👀V",
            "💡 คลิกปุ่ม Start หรือกด Enter เพื่อเริ่มเล่น",
            "🟢 โหมดง่าย (Assist) เปิดอยู่ (กด F2 เพื่อปิด/เปิดใหม่)"
        };
        for(String s: lines){ g.drawString(s, (W-600)/2+20, y); y += lh; }

        if (startBtn != null) {
            startBtn.setVisible(true);
            startBtn.repaint();
        }
    }

    private void drawHelpOverlay(Graphics2D g){
        int W = world.cols * TILE, H = world.rows * TILE;
        g.setColor(new Color(0,0,0,190)); g.fillRect(0, 0, W, H);

        g.setColor(Color.WHITE); g.setFont(getFont().deriveFont(Font.BOLD, 24f));
        String title = "วิธีเล่น Cat in the Pixel Maze"; int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (W - tw)/2, 56);

        g.setFont(getFont().deriveFont(16f));
        int y = 90, lh = 26;
        String[] lines = new String[]{
            "🎯 เป้าหมาย: ไปยังประตูรูปสมุดหัวใจ (มุมขวาล่าง) ก่อนเวลาหมด",
            "🐾 เดิน: ปุ่มลูกศร ← ↑ → ↓",
            "🧰 ไอเทม: ❄️Z แช่แข็ง | 🧪X โจมตีรอบตัว | 🎁C ล่อไฟเหลือง | 🌈B วาร์ป | 🌳N ฟื้นพลัง",
            "🔥 ศัตรู: 🔴เร็ว/ถึก  🔵ช้า  🟡หลงของล่อ",
            "⚠️ อุปสรรค: ฟ้า -เวลา | แดง -HP | ม่วง -คะแนน",
            "💡 กด F2 เปิด/ปิดโหมดง่าย",
            "ปิดคู่มือกด H อีกครั้ง"
        };
        for(String s: lines){ g.drawString(s, 36, y); y += lh; }
        g.setColor(new Color(255,160,200)); g.drawRoundRect(24, 64, W-48, y-64, 16, 16);
    }

    @Override public void addNotify(){ super.addNotify(); requestFocusInWindow(); }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
