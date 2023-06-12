package leo.gptSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;

public class BouncingAnimation extends JPanel {
    private Image image;
    private int xPos;
    private int yPos;
    private int ySpeed;
    private int direction;

    public BouncingAnimation() {
        image = new ImageIcon("logo.png").getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        xPos = 100;
        yPos = 100;
        ySpeed = 1;
        direction = 1;

        Timer timer = new Timer(60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 更新位置
                yPos += ySpeed * direction;

                // 检查边界碰撞
                if (yPos <= 0) {
                    yPos = 0;
                    direction = 1;
                } else if (yPos + image.getHeight(null) >= getHeight()) {
                    yPos = getHeight() - image.getHeight(null);
                    direction = -1;
                }

                // 重新绘制面板
                repaint();
            }
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制图片
        g.drawImage(image, xPos, yPos, null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("弹性动画");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 400);

                BouncingAnimation panel = new BouncingAnimation();
                frame.add(panel);

                frame.setVisible(true);
            }
        });
    }
}
