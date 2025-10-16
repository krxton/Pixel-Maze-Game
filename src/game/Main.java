package game;

import javax.swing.*;

public class Main extends JFrame {
    public Main() {
        super("Pixel Maze");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(new game.engine.GamePanel(this));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
