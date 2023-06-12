package leo.gptSwing;

import javax.swing.*;
import java.awt.*;

public class CombinedFrame extends JFrame {
    public CombinedFrame() {
        setTitle("login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        // 创建登录面板
        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.NORTH);

        // 创建弹性动画面板
        JPanel bouncingPanel = new BouncingAnimation();
        add(bouncingPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("用户名:");
        panel.add(usernameLabel, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 0, 10, 10);

        JTextField usernameField = new JTextField();
        panel.add(usernameField, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel passwordLabel = new JLabel("密码:");
        panel.add(passwordLabel, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 0, 10, 10);

        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(10, 0, 10, 0);

        JButton loginButton = new JButton("登录");
        panel.add(loginButton, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 10, 10, 10);

        JLabel statusLabel = new JLabel();
        panel.add(statusLabel, constraints);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.equals("admin") && password.equals("password")) {
                statusLabel.setText("登录成功！");
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setText("用户名或密码错误！");
                statusLabel.setForeground(Color.RED);
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CombinedFrame::new);
    }
}
