package game.world;

import java.awt.*;
import java.util.*;

public class MazeGenerator {
    public static void generate(World w){
        for(int r=0;r<w.rows;r++) for(int c=0;c<w.cols;c++) w.map[r][c]= World.Tile.WALL;

        boolean[][] visited = new boolean[w.rows][w.cols];
        Deque<Point> st = new ArrayDeque<>();
        st.push(new Point(1,1));
        visited[1][1]=true; w.map[1][1]= World.Tile.FLOOR;
        int[][] dirs={{2,0},{-2,0},{0,2},{0,-2}};
        Random rng=new Random();

        while(!st.isEmpty()){
            Point cur = st.peek();
            java.util.List<int[]> order = new ArrayList<>(java.util.Arrays.asList(dirs));
            java.util.Collections.shuffle(order, rng);
            boolean moved=false;
            for(int[] d: order){
                int nx=cur.x+d[0], ny=cur.y+d[1];
                if (nx>0&&ny>0&&nx<w.cols-1&&ny<w.rows-1 && !visited[ny][nx]){
                    visited[ny][nx]=true;
                    w.map[ny][nx]= World.Tile.FLOOR;
                    w.map[cur.y + d[1]/2][cur.x + d[0]/2]= World.Tile.FLOOR;
                    st.push(new Point(nx,ny));
                    moved=true; break;
                }
            }
            if (!moved) st.pop();
        }
        w.map[w.rows-2][w.cols-2]= World.Tile.EXIT;
    }
}
