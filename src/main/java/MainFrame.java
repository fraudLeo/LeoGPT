import com.jtattoo.plaf.hifi.HiFiLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

public class MainFrame extends JFrame{


    //配置类
    public static Properties properties;
    //配置文件流
    public static FileInputStream fileInputStream;
    //主题选择
    public static int selTheme = 0;
    //主框架!!!
    private static MainFrame frame;
    //设置窗口大小
    private static int FormSize = 3;
    //设置字体大小
    private static int FontSize = 12;






    public static void main(String[] args) {
        //设置整个程序的单线程模型,添加指定事务到时间调度线程
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //设置默认编码,从JVM进行调整
                System.setProperty("file.encoding", "UTF-8");
                /*
                    反射大军!!!!!
                 */
                try {
                    //反射获取Charset这个类的"defaultCharset"字段
                    Field defaultCharset = Charset.class.getDeclaredField("defaultCharset");
                    //绕过限制字段能够进行修改属性
                    defaultCharset.setAccessible(true);
                    defaultCharset.set(null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //开始配置
                properties = new Properties();
                InputStream input = null;

                //加载配置文件
                try {
                    input = new FileInputStream("config.properties");
                    properties.load(input);

                } catch (Exception e) {
                    //当没有找到配置文件的时候,就会弹出是否创建配置文件的窗口
                    int flag = JOptionPane.showConfirmDialog(null, "找不到配置文件,你愿意现在创建一个吗？",
                            "创建配置文件", JOptionPane.YES_NO_OPTION);

                    //如果同意创建
                    if (flag == JOptionPane.YES_OPTION) {
                        //除了这个,其他基本都是默认
                        String apiKey = JOptionPane.showInputDialog(null, "请输入你的APIkey");
                        properties.setProperty("apikey", apiKey);
                        properties.setProperty("model", "gpt-3.5-turbo");
                        properties.setProperty("maxTokens", "1024");
                        properties.setProperty("timeout", "30");
                        properties.setProperty("proxyip", ""); // WIP Support will be added back
                        properties.setProperty("proxyport", ""); // WIP Support will be added back
                        properties.setProperty("proxytype", "");
                        properties.setProperty("autotitle", "true");
                        properties.setProperty("autoscroll", "true");
                        properties.setProperty("EnterToSubmit", "true");
                        properties.setProperty("chat_history", "true");
                        properties.setProperty("chat_location_override", "");
                        properties.setProperty("WindowSize", "medium");
                        properties.setProperty("FontSize", "12");
                        properties.setProperty("Theme", "dark");

                        try {
                            //将上面的文件输出成配置文件
                            FileOutputStream fileOutputStream = new FileOutputStream("config.properties");
                            properties.store(fileOutputStream, "生成配置文件");
                            //关闭流
                            fileOutputStream.close();

                            fileInputStream = new FileInputStream("config.properties");
                            if (fileInputStream != null) {
                                JOptionPane.showMessageDialog(null, "成功创建配置文件");
                                fileInputStream.close();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                //最终都是要关闭输入流的
                finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                setMyProxy(properties);
                //设置主题
                try {
                    setMyTheme(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //加载主页面显示框架
                frame = new MainFrame();
                //从配置文件中获取窗口大小
                switch(properties.getProperty("WindowSize")) {
                    case "small" : FormSize = 1; break;
                    case "large" : FormSize = 2; break;
                    default : FormSize = 3; break;
                }
//                setFormSize();

                //设置logo
//                System.out.println(getClass().toString());
//                frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo.png")));
                //获取配置文件里面的字体数据
                if (properties.getProperty("FontSize") !=null && properties.getProperty("FontSize").isEmpty()) {
                    FontSize = Integer.parseInt(properties.getProperty("FontSize"));
                }
            frame.setVisible(true);
            }
        });

    }

    /**
     * 设置全局大小
     */
    private static void setFormSize() {
//        switch(FormSize){
//            case 1:
//                frame.getContentPane().setPreferredSize(new Dimension(475, 532));
//                frame.pack();
//                scrollPane_1.setBounds(103, 454, 363, 69);
//                scrollPane.setBounds(10, 11, 456, 432);
//                SubmitButton.setBounds(10, 454, 89, 23);
//                SaveButton.setBounds(10, 477, 43, 23);
//                ImportButton.setBounds(56, 477, 43, 23);
//                ResetButton.setBounds(10, 500, 89, 23);
//                break;
//            case 2:
//                frame.getContentPane().setPreferredSize(new Dimension(1370, 960));
//                frame.pack();
//                SubmitButton.setBounds(13, 831, 148, 36);
//                ResetButton.setBounds(13, 914, 148, 36);
//                scrollPane.setBounds(13, 15, 1344, 802);
//                scrollPane_1.setBounds(171, 831, 1186, 118);
//                SaveButton.setBounds(13, 873, 73, 36);
//                ImportButton.setBounds(88, 873, 73, 36);
//                break;
//            default:
//                frame.getContentPane().setPreferredSize(new Dimension(686, 647));
//                frame.pack();
//                SubmitButton.setBounds(10, 554, 89, 23);
//                ResetButton.setBounds(10, 616, 89, 23);
//                scrollPane.setBounds(10, 11, 667, 532);
//                scrollPane_1.setBounds(109, 554, 568, 85);
//                SaveButton.setBounds(10, 585, 43, 23);
//                ImportButton.setBounds(56, 585, 43, 23);
//                break;
//        }
    }

    /**
     * 设置主题
     *
     * @param properties
     */
    private static void setMyTheme(Properties properties) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String theme = properties.getProperty("Theme");
        if (!theme.isEmpty()) {
            if (theme.equals("dark")) {
                Properties p = new Properties();
                //设置了窗口标题的字体为 "Ebrima"，风格为普通（PLAIN），字号为 15。
                p.put("windowTitleFont", "Ebrima PLAIN 15");
                //可能是指禁用了某种背景纹理或图案的显示。
                p.put("backgroundPattern", "off");
                //将 Logo 字符串设置为空，可能是指移除了界面上的 Logo 或者设置为空字符串作为占位符
                p.put("logoString", "");
                HiFiLookAndFeel.setCurrentTheme(p);
                UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                //代表选择了dark
                selTheme = 1;
            }
        }
    }

    /**
     * 设置代理模式
     *
     * @param p
     */
    private static void setMyProxy(Properties p) {
        //设置代理模式
        //先判断代理是不是空
        //新代理ip
        String proxyip = p.getProperty("proxyip");
        //新代理端口
        String proxyport = p.getProperty("proxyport");
        //新代理头
        String proxytype = p.getProperty("proxytype");


        if (proxyip != null && !proxyip.isEmpty() &&
                proxyport != null && !proxyport.isEmpty()) {
            //根据协议分三种情况
            if (proxytype.toLowerCase().equals("http")) {
                System.setProperty("http.proxyHost", proxyip);
                System.setProperty("http.proxyPort", proxyport);
            } else if (proxytype.toLowerCase().equals("https")) {
                System.setProperty("https.proxyHost", proxyip);
                System.setProperty("https.proxyPort", proxyport);
            } else {
                //添加配置
                System.getProperties().put("proxySet", "true");
                System.getProperties().put("socksProxyHost", proxyip);
                System.getProperties().put("socks.ProxyPort", proxyport);
            }
        }

    }
}
