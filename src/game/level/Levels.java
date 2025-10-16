package game.level;

public class Levels {
    public static Level get(int n){
        return switch(n){
            case 1 -> new Level(
                    15, 20,
                    90, 3, 0,       //กำหนดเวลา, ชีวิต, คะแนนเริ่มต้น
                    2, 2, 1, 1,
                    1, 1,          // 🌈, 🌳
                    2, 1, 1,
                    3, 1, 1
            );
            case 2 -> new Level(
                    20, 25,
                    120, 3, 0,
                    2, 3, 1, 2,
                    1, 2,
                    3, 2, 2,
                    5, 2, 2
            );
            case 3 -> new Level(
                    
                    20, 25,            // rows, cols (เดิม 25, 30)
                    150, 3, 0,
                    3, 4, 1, 2,
                    2, 2,
                    
                    4, 3, 3,           // red, blue, yellow (เดิม 5,3,3)
                    5, 2, 2            // timeTrap, spike, curse (เดิม 7,3,3)
            );
            default -> null;
        };
    }
}
