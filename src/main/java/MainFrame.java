import com.google.gson.Gson;
import com.jtattoo.plaf.TitlePane;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.SneakyThrows;
import okhttp3.internal.http2.ErrorCode;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.tools.Tool;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

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
    //创建唯一实例
    //单例模式的目的是确保一个类只有一个实例，
    // 并提供一个全局访问点来获取该实例。
    // 在这种情况下，INSTANCE 变量是用来存储 MainFrame 类的唯一实例。
    private static MainFrame INSTANCE = null;
    //openAI服务接口创建
    private OpenAiService service;
    //菜单栏
    private JMenuBar menuBar;
    //Parser 是一个用于解析用户输入或处理界面数据的工具类或接口。将用户输入转换为特定的数据类型，或者解析和处理界面上的数据模型。
    private static Parser parser;
    //网页渲染模型
    private static HtmlRenderer renderer;
    //判断是否为网页模式显示
    private static Boolean isHtmlView = false;
    //创建可滚动视图区
    private static JScrollPane scrollPane;
    //可用于在图形用户界面中显示和编辑多种类型的富文本内容。日常模式
    private static JEditorPane DisplayArea;
    //富文本格式,网页模式
    private static JEditorPane HTMLArea;
    /**不同部分的字体对象
     */
    //对话中"你"的部分
    private static Style YouStyle;
    private static Style InvisibleStyle;
    //对话中GPT的部分
    private static Style GPTStyle;
    private static Style ChatStyle;
    private static Style ErrorStyle;
    //消息队列!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private final static ArrayList<ChatMessage> messages = new ArrayList<>();
    /**
     * JEditorPane: JEditorPane 是一个用于显示和编辑富文本内容的 Swing 组件。
     * 它是一个通用的文本显示区域，可以显示多种格式的文本，如 HTML、RTF、文本等。
     * JEditorPane 提供了基本的文本编辑功能，如插入、删除、选择文本等，
     * 以及处理超链接和其他与文本内容相关的功能。它是一个可视化的组件，
     * 可以在图形用户界面中显示和交互。
     *
     * StyledDocument: StyledDocument 是一个接口，用于管理富文本文档的样式。
     * 它是 javax.swing.text 包中的一部分，扩展了 Document 接口，
     * 并提供了处理文本样式和属性的方法。StyledDocument 允许您在文档中应用和管理不同的样式，
     * 并将样式应用于指定范围的文本。它提供了更精细的控制和灵活性，使您能够定义和应用多个样式，
     * 如字体、颜色、粗体、斜体等，以创建自定义的富文本文档。
     *
     * 简而言之，JEditorPane 是用于显示和编辑富文本内容的组件，而 StyledDocument 是一个接口，用于管理富文本文档的样式。JEditorPane 可以使用 StyledDocument 实现对文本样式的控制和管理，通过将不同的 StyledDocument 实例设置给 JEditorPane，可以实现不同样式的文本显示和编辑。
     */
    //处理富文本的精细控制
    private static StyledDocument doc;
    //和GPT的对话文件进行保存,关键功能!!!!!!!!!
    private File gptSaveFile;
    private File gptSaveFile_2;
    //设置提交按钮
    private static JButton SubmitButton;
    //查看关于页面是否展开
    private Boolean aboutShow = false;
    //判断加载文件窗口是否打开
    private Boolean cloaderopen = false;
    //另一个页面的对象,是加载文件窗口
    private ChatLoader cloader;
    //加载文件的加载目录
    private String chatDir;
    //组件块儿,主要的东西!!!!!!!!!!!!!!!!!!!!!!!
    private JPanel contentPane;
    //创建滚动
    private JScrollPane scrollPane_1;
    //判断输入流是否还在
    private Boolean isStreamRunning = false;
    //输入的文本放到对话框里的对象
    public static JTextArea CharArea;
    //GPT临时的信息回复
    private static String GPTConvo;
    //聊天历史判断
    private Boolean chathistory = true;
    //判断是不是之前没有聊天历史记录,是第一个就是真,就往下执行
    private Boolean first = true;
    //判断是否设立自动取标题
    private Boolean autotitle = true;
    //保存按钮
    private JButton saveButton;
    //导入按钮
    private JButton jButton;
    //开启二次提交
    private Boolean enter2submit = true;




    public MainFrame() {
        //不允许窗口的大小发生自定义变化, 可能也是和自适应有关,这边先设置不能够更改
        setResizable(false);
        //将单例指向自己
        INSTANCE = this;
        contentPane = new JPanel();
        //设置标题
        setTitle("ARK");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //根据所提供的API,初始化GPT
        service =  new OpenAiService(properties.getProperty("apikey"),properties.getProperty("timeout") == null
                && properties.getProperty("timeout")
                .isEmpty()? Duration.ZERO : Duration.ofSeconds(Long.parseLong(properties.getProperty("timeout"))));

        //------------------------------------------------------------------------------------------
        //设置上下左右的边框
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(contentPane);
        //不使用风格
        contentPane.setLayout(null);
        //创建滚动区域
        scrollPane = new JScrollPane();
        contentPane.add(scrollPane);
        //创建展示区域,默认和HTML显示
        DisplayArea = new JEditorPane();
        DisplayArea.setEditable(false);
        DisplayArea.setContentType("text/rft");
//        scrollPane.setViewportView(DisplayArea);

        HTMLArea = new JEditorPane();
        HTMLArea.setEditable(false);
        HTMLArea.setContentType("text/html");
        HTMLArea.setBackground(Color.white);
        scrollPane.setViewportView(HTMLArea);

        //这里开始是自己的文本格式
        StyleContext sc = StyleContext.getDefaultStyleContext();
        //字的重量
        YouStyle = sc.addStyle("bold",null);
        //字的字体
        StyleConstants.setFontFamily(YouStyle,"Tahoma");
        //字的大小
        StyleConstants.setFontSize(YouStyle,FontSize);
        //感觉下面的没有也行
//        StyleConstants.setBold(YouStyle,true);

        //下面开始设置GPT的文本格式
        GPTStyle = sc.addStyle("bold",null);
        StyleConstants.setFontFamily(GPTStyle,"Tahoma");
        StyleConstants.setFontSize(GPTStyle,FontSize);
        StyleConstants.setBold(GPTStyle,true);
        //设置字的颜色
        StyleConstants.setForeground(GPTStyle,Color.RED);


        InvisibleStyle = sc.addStyle("bold", null);
        StyleConstants.setForeground(InvisibleStyle, DisplayArea.getBackground());

        ChatStyle = sc.addStyle("black", null);
        StyleConstants.setFontFamily(ChatStyle, "Tahoma");
        StyleConstants.setFontSize(ChatStyle, FontSize);

        ErrorStyle = sc.addStyle("ErrorStyle", null);
        StyleConstants.setItalic(ErrorStyle, true);
        StyleConstants.setFontFamily(ErrorStyle, "Tahoma");
        StyleConstants.setFontSize(ErrorStyle, FontSize);


        if(selTheme == 1) {
            StyleConstants.setForeground(YouStyle, Color.ORANGE); //getHSBColor(30f/360, 0.8f, 1f)
            StyleConstants.setForeground(ChatStyle, Color.WHITE); //Color.getHSBColor(0f, 0f, 0.8f)
            StyleConstants.setForeground(ErrorStyle, Color.WHITE); //Color.getHSBColor(0f, 0f, 0.8f)
        }else {
            StyleConstants.setForeground(YouStyle, Color.BLUE);
            StyleConstants.setForeground(ChatStyle, Color.BLACK);
            StyleConstants.setForeground(ErrorStyle, Color.BLACK);
        }
        //------------------------------------------------------------------------------------------

        //获取文本内容!!!!!!!!!!!!
        doc = (StyledDocument) DisplayArea.getDocument();
        //------------------------------------------------------------------------------------------

        //设置点击提交按钮
        SubmitButton = new JButton("提交");
        SubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });

        //保存按钮
        saveButton = new JButton("保存");
        saveButton.setFont(new Font("Arial Black",Font.BOLD, 6));
        saveButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                //打开当前目录
                File defaultDir = new File(".");
                //选择文件保存路径
                JFileChooser jFileChooser = new JFileChooser(defaultDir);
                jFileChooser.setDialogTitle("保存对话");
                //弹出窗口
                int res = jFileChooser.showSaveDialog(null);
                if (res == jFileChooser.APPROVE_OPTION) {
                    File selectedFile = jFileChooser.getSelectedFile();

                    FileWriter fileWriter = new FileWriter(selectedFile);

                    String plainText = DisplayArea.getDocument().getText(0,DisplayArea.getDocument().getLength());
                    fileWriter.write(plainText);
                    fileWriter.close();
                    JOptionPane.showMessageDialog(null,"文件保存成功");
                }
            }
        });

        //将保存按钮添加到视图
        contentPane.add(saveButton);
        //------------------------------------------------------------------------------------------

        //添加导入按钮
        jButton = new JButton("保存");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser(new File("."));
                jFileChooser.setDialogTitle("导入聊天文件");
                int res = jFileChooser.showOpenDialog(null);
                if (res ==JFileChooser.APPROVE_OPTION) {
                    String absoluteFile = jFileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        CharArea.setText(new String(Files.readAllBytes(Paths.get(absoluteFile))));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        jButton.setIcon(new ImageIcon("upFolder.gif"));
        contentPane.add(jButton);

        //------------------------------------------------------------------------------------------
        //监听鼠标事件
        DisplayArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    //展示鼠标右键功能
                    showDisplayMenu(e.getX(),e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    //展示鼠标右键功能
                    showDisplayMenu(e.getX(),e.getY());
                }
            }
        });

        HTMLArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showHTMLMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showHTMLMenu(e.getX(), e.getY());
                }
            }
        });

        CharArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showChatMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showChatMenu(e.getX(), e.getY());
                }
            }
        });

        //------------------------------------------------------------------------------------------
        //创建菜单栏
        menuBar = new JMenuBar();
        //创建菜单栏选项
        JMenu optionMenu = new JMenu("选项");
        menuBar.add(optionMenu);

        //以网页模式展示
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();

        //状态切换
        JMenuItem htmlView = new JMenuItem("网页展示");
        //添加监听器
        htmlView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isHtmlView) {
                    //将富文本变成可滚动区域
                    scrollPane.setViewportView(DisplayArea);
                    htmlView.setText("切换展示");
                    isHtmlView = false;
                } else {
                    scrollPane.setViewportView(HTMLArea);
                    resetHTMLAreaStyle();
                    try {
                        //文本框中的将数据对象转换成文本
                        parser.parse(DisplayArea.getDocument().getText(0,DisplayArea.getDocument().getLength()));
                        HTMLArea.setText("正常模式");
                        isHtmlView = true;
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        //添加模式切换功能
        optionMenu.add(htmlView);
        //------------------------------------------------------------------------------------------

        //修改页面大小
        JMenu formSize = new JMenu("页面大小");
        optionMenu.add(formSize);

        //小型窗口的item
        JMenuItem smallView = new JMenuItem("小型窗口");
        formSize.add(smallView);
        smallView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FormSize!=1) {
                    FontSize = 1;
                    setFormSize();
                }
            }
        });
        //中等窗口的item
        JMenuItem mediumView = new JMenuItem("中型窗口");
        formSize.add(mediumView);
        mediumView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FontSize!=2) {
                    FontSize = 2;
                    setFormSize();
                }
            }
        });
        //大型窗口的item
        JMenuItem largeView = new JMenuItem("大型窗口");
        formSize.add(largeView);
        largeView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FontSize!=3) {
                    FontSize = 3;
                    setFormSize();
                }
            }
        });
        //------------------------------------------------------------------------------------------

        //设置字体
        JMenu fontSize = new JMenu("字体大小");
        optionMenu.add(fontSize);
        //默认字体
        JMenuItem defaultSize = new JMenuItem("默认大小 (12)");
        fontSize.add(defaultSize);
        defaultSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FontSize!=12) {
                    FontSize = 12;
                    setFontSize();
                    refreshMessages();
                }
            }
        });
        //大字体
        JMenuItem largeSize = new JMenuItem("大字体 (16)");
        fontSize.add(largeSize);
        largeSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FontSize != 16) {
                    FontSize = 16;
                    setFontSize();
                    refreshMessages();
                }
            }
        });
        //超大字体
        JMenuItem exLargeSize = new JMenuItem("超大字体 (20)");
        fontSize.add(exLargeSize);
        exLargeSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FontSize!=20){
                    FontSize = 20;
                    setFontSize();
                    refreshMessages();
                }
            }
        });
        //自定义字体
        JMenuItem customSize = new JMenuItem("自定义");
        fontSize.add(customSize);
        customSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer fs = Integer.valueOf(JOptionPane.showInputDialog(null,"请输入你的字号:","自定义字体",JOptionPane.PLAIN_MESSAGE));
                if (FontSize!= fs) {
                    FontSize = fs;
                    setFontSize();;
                    refreshMessages();
                }
            }
        });
        //------------------------------------------------------------------------------------------

        //保存对话文件(前瞻,只是设置保存文件的名称样式),这部分的作用用于切换模式,状态保存模式.另一份日志或者文件提取在另一part
        JMenu fileName = new JMenu("文件命名");
        optionMenu.add(fileName);
        //自动命名
        JMenuItem auto = new JMenuItem("自动");
        fileName.add(auto);
        auto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gptSaveFile != null) {
                    AutoTitle();
                } else {
                    JOptionPane.showMessageDialog(null,"没有聊天记录文件","Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //自定义命名
        JMenuItem custom = new JMenuItem("自定义");
        fileName.add(custom);
        custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gptSaveFile != null) {
                    //代表的是No icon is used.
                    String title = JOptionPane.showInputDialog(null, "请输入标题", "命名", JOptionPane.PLAIN_MESSAGE);
                    if (title!= null) {
                        //以json格式保存,方便将来扩展功能传输信息
                        File file = new File(gptSaveFile.getParentFile(), title + ".json");
                        //判断重名或者存在
                        if (!file.exists()) {
                            //首先重命名
                            gptSaveFile.renameTo(file);
                            //再将对象属性给他
                            gptSaveFile = file;
                            //表示成功,Used for information messages.
                            JOptionPane.showMessageDialog(null,"成功创建文件","Success",JOptionPane.INFORMATION_MESSAGE);
                            //重命名实例标题名称
                            INSTANCE.setTitle("GPT-"+title);
                        } else {
                            JOptionPane.showMessageDialog(null,"???我也不知道出了啥错误","Error",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null,"未找到文件","Error",JOptionPane.ERROR_MESSAGE);
                }


            }
        });

        //------------------------------------------------------------------------------------------
        //操作上面的文件,用于删除记录
        JMenu delete = new JMenu("删除当前对话");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gptSaveFile !=null && gptSaveFile.exists()) {
                    gptSaveFile.delete();
                    reset();
                } else {
                    JOptionPane.showMessageDialog(null, "文件没有找到","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menuBar.add(delete);
        //------------------------------------------------------------------------------------------

        //退回当前对话
        JMenu revert = new JMenu("退后");
        revert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (messages.size()>=2) {//因为一来一回,一个问题两个人要说话,所以退回版本得退两次
                    //弹出去
                    messages.remove(messages.size()-1);
                    messages.remove(messages.size()-1);
                    //状态得进行一次刷新
                    refreshMessages();
                } else {
                    JOptionPane.showMessageDialog(null,"消息数量太少了,不需要删除","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menuBar.add(revert);
        //------------------------------------------------------------------------------------------
        JMenu aboutMenu = new JMenu("关于");
        aboutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //关于信息,新建一个包装类
                About about = new About();
                //这种方式适用于加载本地文件或远程 URL 中的图像。直接输入url适合在网页里面操作.
                about.setIconImage(Toolkit.getDefaultToolkit().getImage("log.png"));
                about.setVisible(true);
                about.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        aboutShow = false;
                    }
                });
            }
        });
        menuBar.add(aboutMenu);
        //------------------------------------------------------------------------------------------
        //加载记录
        JMenu load = new JMenu("加载记录");
        load.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (cloaderopen != true) {
                    cloader = new ChatLoader(chatDir);
                    cloader.setVisible(true);
                    cloader.setIconImage(Toolkit.getDefaultToolkit().getImage("log.png"));
                    cloaderopen = true;
                    cloader.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            cloaderopen = false;
                        }
                    });
                }
            }
        });
        menuBar.add(load);

        //------------------------------------------------------------------------------------------
        //允许html文本超链接
        HTMLArea.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        //------------------------------------------------------------------------------------------
        //Bulk property setting-------------------
        try {
            if(properties.getProperty("autoscroll") != null && !properties.getProperty("autoscroll").isEmpty()) {
                if(properties.getProperty("autoscroll").equals("true")) {
                    DefaultCaret caret = (DefaultCaret)DisplayArea.getCaret();
                    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                }
            }

            if(properties.getProperty("chat_history") != null && !properties.getProperty("chat_history").isEmpty()) {
                if(properties.getProperty("chat_history").equals("true")){
                    chathistory = true;
                }else{
                    chathistory = false;
                }
            }

            if(properties.getProperty("autotitle") != null && !properties.getProperty("autotitle").isEmpty()) {
                if(properties.getProperty("autotitle").equals("true")){
                    autotitle = true;
                }else{
                    autotitle = false;
                }
            }

            if(properties.getProperty("EnterToSubmit") != null && !properties.getProperty("EnterToSubmit").isEmpty()) {
                if(properties.getProperty("EnterToSubmit").equals("true")){
                    CharArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none");
                }else{
                    enter2submit = false;
                }
            }


            if(properties.getProperty("chat_location_override") != null && !properties.getProperty("chat_location_override").isEmpty()){
                chatDir = properties.getProperty("chat_location_override");
            }else {
                try {
                    chatDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
                    chatDir = chatDir + "\\chat_history";
                    File directory = new File(chatDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch (URISyntaxException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            //----------------------------------------
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    private void showChatMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = CharArea.getSelectedText();
                if (selectedText != null) {
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                }
            }
        });


        popupMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = CharArea.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents(null);
                    if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            String clipboardText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                            CharArea.replaceSelection(clipboardText);
                        } catch (UnsupportedFlavorException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents(null);
                    if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            String clipboardText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                            int caretPos = CharArea.getCaretPosition();
                            CharArea.insert(clipboardText, caretPos);
                        } catch (UnsupportedFlavorException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        popupMenu.add(pasteMenuItem);

        JMenuItem clearMenuItem = new JMenuItem("Clear");
        clearMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CharArea.setText("");
            }
        });
        popupMenu.add(clearMenuItem);

        popupMenu.show(CharArea, x, y);
    }

    private void showHTMLMenu(int x, int y) {

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = HTMLArea.getSelectedText();
                if (selectedText != null) {
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                }
            }
        });
        popupMenu.add(copyMenuItem);
        popupMenu.show(HTMLArea, x, y);
    }


    //展示鼠标功能
    private void showDisplayMenu(int x, int y) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = DisplayArea.getSelectedText();
                if (selectedText!=null) {
                    StringSelection stringSelection = new StringSelection(selectedText);
                    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    systemClipboard.setContents(stringSelection,null);
                }
            }
        });

        jPopupMenu.add(copy);
        jPopupMenu.show(DisplayArea, x,y);
    }

    //提交按钮
    private void submit() {
        //倘若标识显示的是true,也得给我改回来,用于多次提问
        if(isStreamRunning) {
            isStreamRunning = false;
            //设置标签名称
            SubmitButton.setText("Submit");
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                SubmitButton.setText("终止提问");
                doc.insertString(doc.getLength(),"You",YouStyle);
                doc.insertString(doc.getLength(),":\n",InvisibleStyle);
                doc.insertString(doc.getLength(),CharArea.getText() + "\n\n",ChatStyle);
                doc.insertString(doc.getLength(),"CuteBot",GPTStyle);
                doc.insertString(doc.getLength(),":\n",InvisibleStyle);

                StringBuilder stringBuilder = new StringBuilder();
                //获取user的输入对象,并添加到messages里面
                final ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), CharArea.getText());
                messages.add(chatMessage);
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .maxTokens(Integer.parseInt(properties.getProperty("maxTokens")))
                        .n(1)
                        .messages(messages)
                        .model(properties.getProperty("model"))
                        .logitBias(new HashMap<>())
                        .build();
                //设置状态,开始读流
                /*takeWhile操作符会在满足给定条件的情况下，从流中获取元素，一旦条件不满足，它将停止获取后续的元素。
                    在这个例子中，res -> isStreamRunning是一个Lambda表达式，用于定义条件函数。
                    它使用isStreamRunning变量来判断是否继续取元素
                 */
                isStreamRunning = true;
                service.streamChatCompletion(chatCompletionRequest)
                        .doOnError(Throwable::printStackTrace)
                        .takeWhile(res -> isStreamRunning)
                        .blockingForEach(chunk -> {
                            for (ChatCompletionChoice choice : chunk.getChoices()) {
                                if (choice.getMessage().getContent() != null) {
                                    stringBuilder.append(choice.getMessage().getContent());
                                }
                                doc.insertString(doc.getLength(), choice.getMessage().getContent(),ChatStyle);

                            }
                        });
                if (isStreamRunning) {
                    doc.insertString(doc.getLength(),"\n\n",ChatStyle);
                    if (isHtmlView) {
                        resetHTMLAreaStyle();
                        Node document = parser.parse(DisplayArea.getDocument().getText(0, DisplayArea.getDocument().getLength()));
                        HTMLArea.setText(renderer.render(document));
                    }
                    //获取到GPT说的内容
                    GPTConvo = stringBuilder.toString();
                    final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), GPTConvo);
                    messages.add(systemMessage);

                    if (chathistory) {
                        if (first) {
                            //如果是有历史记录并且当前是第一个(存疑
                            newFile();
                        }
                        writeMessagesToFile(gptSaveFile_2.getPath());
                        if (first&&autotitle) {
                                AutoTitle();
                                first = false;
                        }
                    }
                    //清空当前
                    CharArea.setText("");
                } else {
                    if (messages.size()!=0) {
                        messages.remove(messages.size() - 1);
                        doc.insertString(doc.getLength(), "\n\n" + "之前的提示和回应因为取消所以没有保存"+"\n", ErrorStyle);
                    }
                }
                isStreamRunning = false;
                SubmitButton.setText("提交");
            }
        });
        thread.start();
    }

    //写入文件规定格式
    private void writeMessagesToFile(String path) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(path)) {
            Gson gson = new Gson();
            for (ChatMessage message : messages) {
                String json = gson.toJson(message);
                writer.println(json);
            }
        }

    }

    //通过GPT对话文件创建一个新的文件
    private void newFile() {
        String randFileName = getRandomString();
        gptSaveFile_2 = new File(chatDir + "\\Chat_" + randFileName + ".json");
        //如果文件名一直重复
        while(gptSaveFile_2.exists()) {
            randFileName = getRandomString();
            gptSaveFile_2 = new File(chatDir + "\\Chat_" + randFileName + ".json");
        }
        setTitle("JavaGPT - Chat_" + randFileName);
    }

    //创建新的文件名,(随即创建)
    private String getRandomString() {
        String letters = "abcdefghijklmnopqrstuvwxyz1234567890";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            stringBuilder.append(letters.charAt(random.nextInt(letters.length())));
        }
        return stringBuilder.toString();
    }

    /**
     * 用于重置当前状态
     */
    private void reset() {
        isStreamRunning = false;
        messages.clear();
        gptSaveFile = null;
        GPTConvo = "";
        DisplayArea.setText("");
        HTMLArea.setText("");
        resetHTMLAreaStyle();
        CharArea.setText("");
        setTitle("JavaGPT");
        first = true;
    }

    /**
     * 自定义对话标题
     */
    private void AutoTitle() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                setTitle("程序正在生成标题,请耐心等待");
                SubmitButton.setText("加载中");
                StringBuilder stringBuilder = new StringBuilder();
                //解铃还须系铃人,利用gpt总结当前对话
                final ChatMessage sysMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "为当前对话创建简易标题概括");
                //将得到的messege添加到本来的消息列表里面
                messages.add(sysMessage);
                //
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model(properties.getProperty("model"))
                        .messages(messages)
                        .n(1)
                        .maxTokens(25)
                        .logitBias(new HashMap<>())
                        .build();
                service.streamChatCompletion(chatCompletionRequest)
                        .doOnError(Throwable::printStackTrace)
                        .blockingForEach(chunk -> {
                            for (ChatCompletionChoice choice : chunk.getChoices()) {
                                if (choice.getMessage().getContent()!=null) {
                                    stringBuilder.append(choice.getMessage().getContent());

                                }
                            }
                        });
                messages.remove(messages.size() - 1);
                String title = stringBuilder.toString();
                //将特殊符号转换为空,方便保存名字
                title = title.replaceAll("[\\\\/:*?\"<>|]", "");
                if(title.substring(title.length() - 1 ).equals(".")) {
                    title = title.substring(0,title.length()- 1);
                }
                SubmitButton.setText("提交");
                if (title!=null) {
                    File file = new File(gptSaveFile.getParentFile(), title + ".json");
                    if (file.exists()) {
                        JOptionPane.showMessageDialog(null,"文件已存在","Error",JOptionPane.ERROR_MESSAGE);
                        setTitle("CuteGPT - " + gptSaveFile.getName().substring(0,gptSaveFile.getName().length()-5));
                    }
                    else {
                        gptSaveFile.renameTo(file);
                        gptSaveFile = file;
                        INSTANCE.setTitle("CuteGPT - " + gptSaveFile.getName().substring(0,gptSaveFile.getName().length()-5));
                    }
                }
            }
        });
        thread.start();
    }


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
                frame.setIconImage(Toolkit.getDefaultToolkit().getImage("logo.png"));
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

    //将文本正确显示为html,需要先将他转换为普通文本,这样转换地更加彻底
    private static void resetHTMLAreaStyle() {
        HTMLArea.setContentType("text/plain");
        HTMLArea.setContentType("text/html");
    }

    /**
     * 设置不同模块的字体大小,进行全局更改
     */
    public void setFontSize() {
        StyleConstants.setFontSize(YouStyle, FontSize);
        StyleConstants.setFontSize(GPTStyle, FontSize);
        StyleConstants.setFontSize(ChatStyle, FontSize);
        StyleConstants.setFontSize(ErrorStyle, FontSize);
    }

    /**
     * 刷新当前消息记录
     */
    public void refreshMessages() {
        DisplayArea.setText("");
        //ChatMessage是openAI的类,封装了role和content两个
        for (ChatMessage message : messages) {
            if(message.getRole().equals("user")) {
                try {
                    doc.insertString(doc.getLength(), "You", YouStyle);
                    doc.insertString(doc.getLength(), ":\n", InvisibleStyle);
                    doc.insertString(doc.getLength(), message.getContent() + "\n\n", ChatStyle);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    doc.insertString(doc.getLength(), "ChatGPT", GPTStyle);
                    doc.insertString(doc.getLength(), ":\n", InvisibleStyle);
                    doc.insertString(doc.getLength(), message.getContent() + "\n\n", ChatStyle);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
