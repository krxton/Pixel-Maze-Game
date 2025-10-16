package game.world;

import game.entities.*;
import game.items.*;
import game.obstacles.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class World {
    public enum Tile { FLOOR, WALL, EXIT }

    public final int rows, cols;
    public Tile[][] map;
    public List<Item> items = new ArrayList<>();
    public List<Enemy> enemies = new ArrayList<>();
    public List<Obstacle> obstacles = new ArrayList<>();
    public Player player;
    public BufferedImage exitSprite;

    public World(BufferedImage exitSprite, int rows, int cols){
        this.rows=rows; this.cols=cols; this.exitSprite=exitSprite;
        map = new Tile[rows][cols];
    }

    public boolean inside(int x,int y){ return x>=0 && y>=0 && x<cols && y<rows; }
    public boolean passable(int x,int y){ return inside(x,y) && map[y][x]!=Tile.WALL; }
    public boolean isExit(int x,int y){ return inside(x,y) && map[y][x]==Tile.EXIT; }

    public void draw(Graphics2D g, int TILE){
        for(int r=0;r<rows;r++){
            for(int c=0;c<cols;c++){
                g.setColor(new Color(20,20,20)); g.fillRect(c*TILE, r*TILE, TILE, TILE);
                g.setColor(new Color(35,35,35)); g.drawRect(c*TILE, r*TILE, TILE, TILE);
                if (map[r][c]==Tile.WALL){
                    g.setColor(new Color(60,60,80)); g.fillRect(c*TILE, r*TILE, TILE, TILE);
                }
                if (map[r][c]==Tile.EXIT){
                    if (exitSprite!=null)
                        g.drawImage(exitSprite, c*TILE, r*TILE, TILE, TILE, null);
                    else { g.setColor(new Color(160,80,120)); g.fillRect(c*TILE, r*TILE, TILE, TILE); }
                }
            }
        }

        // วาด obstacles (ถ้าไม่มีสไปรต์ ใช้สี่เหลี่ยมสีแทน)
        for (Obstacle ob: obstacles){
            if (ob instanceof game.obstacles.TimeTrap){
                g.setColor(new Color(60,120,160));
                g.fillRect(ob.x*TILE, ob.y*TILE, TILE, TILE);
            } else if (ob instanceof game.obstacles.Spike){
                g.setColor(new Color(160,60,60));
                g.fillRect(ob.x*TILE, ob.y*TILE, TILE, TILE);
            } else if (ob instanceof game.obstacles.CurseTile){
                g.setColor(new Color(120,60,140));
                g.fillRect(ob.x*TILE, ob.y*TILE, TILE, TILE);
            } else {
                ob.draw(g, TILE);
            }
        }

        for (Item it: items) it.draw(g, TILE);
        for (Enemy e: enemies) e.draw(g, TILE);
        if (player!=null) player.draw(g, TILE);
    }
}
