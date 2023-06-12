package leo.gptSwing;

import com.jtattoo.plaf.hifi.HiFiLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class AdvancedLoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public AdvancedLoginFrame() {
        Properties p = new Properties();
        //设置了窗口标题的字体为 "Ebrima"，风格为普通（PLAIN），字号为 15。
        p.put("windowTitleFont", "Ebrima PLAIN 15");
        //可能是指禁用了某种背景纹理或图案的显示。
        p.put("backgroundPattern", "off");
        //将 Logo 字符串设置为空，可能是指移除了界面上的 Logo 或者设置为空字符串作为占位符
        p.put("logoString", "");
        HiFiLookAndFeel.setCurrentTheme(p);
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setTitle("登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

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

        usernameField = new JTextField();
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

        passwordField = new JPasswordField();
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

        statusLabel = new JLabel();
        panel.add(statusLabel, constraints);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.equals("admin") && password.equals("password")) {
                    statusLabel.setText("登录成功！");
                    statusLabel.setForeground(Color.GREEN);
                } else {
                    statusLabel.setText("用户名或密码错误！");
                    statusLabel.setForeground(Color.RED);
                }
            }
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AdvancedLoginFrame();
            }
        });
    }
}
