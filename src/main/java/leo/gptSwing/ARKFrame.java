package leo.gptSwing;

import com.google.gson.Gson;
//import com.jtattoo.plaf.darkstar.DarkStarLookAndFeel;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aero.AeroLookAndFeel;
import com.jtattoo.plaf.graphite.GraphiteLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.luna.LunaLookAndFeel;
import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import com.jtattoo.plaf.mint.MintLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.SneakyThrows;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class ARKFrame extends JFrame {

    /**
     * 主要部件
     */
    //主框架
    private static ARKFrame frame;
    //主容器
    private JPanel contentPane;
    //接口对象创建
    private OpenAiService service;
    //消息队列
    private final static ArrayList<ChatMessage> messages = new ArrayList<>();
    //外部窗口_1
    private ChatLoader cloader;


    /**
     * 操作对象_1
     * 用于操作文字
     */
    //聊天文本区(输入字的地方)
    private static JTextArea ChatArea;
    //提交按钮
    private static JButton SubmitButton;
    //创建滚动容器
    private static JScrollPane scrollPane;
    //创建第二个滚动容器
    private static JScrollPane scrollPane_1;
    //建立保存按钮
    private static JButton SaveButton;
    //建立导入按钮
    private static JButton ImportButton;
    //建立重置按钮
    private static JButton ResetButton;
    //创建实例模式
    private static ARKFrame INSTANCE = null;

    /**
     * 操作对象_2
     * 用于操作顶部工具栏
     */
    //建立工具栏
    private JMenuBar menuBar;
    //是否加载聊天加载区
    private Boolean cloaderopen = false;
    //是否加载关于弹窗
    private Boolean aframeopen = false;
    //是否加载HTML显示
    private static Boolean isHTMLView = false;
    //聊天记录的文件路径
    private String chatDir;
    //创建选项菜单
    private JMenu OptionMenu;

    /**
     * 对话区域_1
     * 重点是GPT的操作
     */
    //对话文本区容器(现实的区域
    private static JEditorPane DisplayArea;
    //倘若以H5的形式展现是使用这个容器对象
    private static JEditorPane HTMLArea;
    //获取GPT的doc文本,因为他的格式不一定,所以用富文本对象
    private static StyledDocument doc;
    //获取GPT当前的交流对象
    private static String GPTConvo;
    //GPT文件流的保存
    private File FGPTConvo;
    //GPT对话的每个节点操作对象工具
    private static Parser parser;
    //html渲染对象
    private static HtmlRenderer renderer;
    //请求的GPT输入流是不是在运行,(关键!!!!!)
    private Boolean isStreamRunning = false;
    //配置文件流
    public static FileInputStream fileInputStream;


    /**
     * 对话区域_2
     * 重点是富文本区域的文字处理
     */
    //用户的风格
    private static Style YouStyle;
    //细节风格
    private static Style InvisibleStyle;
    //gpt的风格
    private static Style GPTStyle;
    //输入的聊天风格
    private static Style ChatStyle;
    //报错风格
    private static Style ErrorStyle;

    /**
     * 配置文件的操作
     */
    //配置文件对象
    public static Properties properties;
    //版本
    public static String version = "1.0.0";
    //是否是第一次对话
    private Boolean first = true;
    //是否存在聊天历史记录
    private Boolean chathistory = true;
    //是否设置自动命名文件
    private Boolean autotitle = true;
    //是否二次提交问题
    private Boolean enter2submit = true;
    //判断是否是字母
    public static Boolean isAlpha = true;
    //总体框大小
    private static int FormSize = 3;
    //字体大小
    private static int FontSize = 12;
    //选择主题
    public static int seltheme = 0;

    /**
     * 主运行代码
     */
    public static void main(String[] args) {
        //设置整个程序的单线程模型,添加指定事务到时间调度线程
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //设置默认编码,从JVM进行调整
                setUniCode();
                //开始操作配置
               setSettings();

                setMyProxy(properties);
                //设置主题
                try {
                    setMyTheme(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //加载主页面显示框架
                frame = new ARKFrame();
                //从配置文件中获取窗口大小
                switch(properties.getProperty("WindowSize")) {
                    case "small" : FormSize = 1; break;
                    case "large" : FormSize = 2; break;
                    default : FormSize = 3; break;
                }
                //设置页面格式
                setFormSize();
                //设置logo
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
     * 无参构造
     */
    public ARKFrame() {
        //不允许窗口的大小发生自定义变化, 可能也是和自适应有关,这边先设置不能够更改
        setResizable(false);
        //将单例指向自己
        INSTANCE = this;
        //创建主容器
        contentPane = new JPanel();
        //创建滚动窗口
        scrollPane_1 = new JScrollPane();
        contentPane.add(scrollPane_1);
        ChatArea = new JTextArea();
        //setWrapStyleWord方法用于指定是否应在行太长而无法适合文本区域的分配宽度时换行。
        //如果设置为true，则行将在单词边界处而不是字符边界处换行。如果设置为false，则行将在字符边界处换行。
        ChatArea.setWrapStyleWord(true);
        //判断是否到达长度,有就下降到下一行
        ChatArea.setLineWrap(true);
        //创建快捷热键,回车键
        ChatArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(enter2submit) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
                        int caret = ChatArea.getCaretPosition();
                        ChatArea.insert("\n", caret);
                        ChatArea.setCaretPosition(caret + 1);
                    }else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                        submit();
                    }
                }else {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                        submit();
                    }
                }
            }
        });
        scrollPane_1.setViewportView(ChatArea);
//        contentPane.setComponentZOrder(ChatArea, 0);
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();

        //设置标题
        setTitle("ARK");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //根据所提供的API,初始化GPT
        service =  new OpenAiService(properties.getProperty("apikey"),properties.getProperty("timeout") == null
                && properties.getProperty("timeout")
                .isEmpty()? Duration.ZERO : Duration.ofSeconds(Long.parseLong(properties.getProperty("timeout"))));

        /**
         * 设置主容器
         */
        setMainContentPane();
        //获取文本内容
        doc = (StyledDocument) DisplayArea.getDocument();
        //新建聊天
        setNewChat();
        //设置保存按钮
        setSaveButton();
        //设置导入系统
        setLoadButton();
        //设置右键功能

        //创建菜单按钮
        setMenu();

        //用户使用界面
        //提交按钮
        setSubmitButtom();
        setRightMouse();
        //批量属性设定
        setBulkProperties();
        //------------------------------------


        //------------------------------------


        //--------------------------------------------------------------------

        //允许在HTML网页模式下超链接的点击事件执行,主要面对GPT4
        HTMLArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * 从文件中读取信息
     * @param filename
     * @throws IOException
     */
    public static void readMessagesFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            Gson gson = new Gson();
            while ((line = reader.readLine()) != null) {
                ChatMessage message = gson.fromJson(line, ChatMessage.class);
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
                messages.add(message);
            }
        }
    }
    /**
     * 加载文件
     * @param fullfilepath
     * @param filename
     * @throws BadLocationException
     */
    public static void loadchat(String fullfilepath, String filename) throws BadLocationException {

        INSTANCE.setTitle("JavaGPT - " + filename);
        try {

            DisplayArea.setText("");

            messages.clear();
            readMessagesFromFile(fullfilepath);
            if(isHTMLView) {
                resetHTMLAreaStyle();
                Node document = parser.parse(DisplayArea.getDocument().getText(0, DisplayArea.getDocument().getLength()));
                //System.out.println(renderer.render(document));
                HTMLArea.setText(renderer.render(document));
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        INSTANCE.FGPTConvo = new File(fullfilepath);

        INSTANCE.first = false;

    }

    /**
     * 加载配置文件
     */
    private void setBulkProperties() {

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
                    ChatArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none");
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

    /**
     * 设置右键功能
     */
    private void setRightMouse() {

        DisplayArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showDisplayMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showDisplayMenu(e.getX(), e.getY());
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

        ChatArea.addMouseListener(new MouseAdapter() {
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
    }

    /**
     * 右键菜单
     * @param x
     * @param y
     */
    private void showDisplayMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem("复制");
        copyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = DisplayArea.getSelectedText();
                if (selectedText != null) {
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                }
            }
        });
        popupMenu.add(copyMenuItem);
        popupMenu.show(DisplayArea, x, y);
    }

    /**
     * 设置HTML菜单
     * @param x
     * @param y
     */
    private void showHTMLMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem("复制");
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

    /**
     * 输入框的右键菜单
     * @param x
     * @param y
     */
    private void showChatMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copyMenuItem = new JMenuItem("复制");
        copyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = ChatArea.getSelectedText();
                if (selectedText != null) {
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
                }
            }
        });
        popupMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem("粘贴按钮");
        pasteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = ChatArea.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable contents = clipboard.getContents(null);
                    if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            String clipboardText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                            ChatArea.replaceSelection(clipboardText);
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
                            int caretPos = ChatArea.getCaretPosition();
                            ChatArea.insert(clipboardText, caretPos);
                        } catch (UnsupportedFlavorException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        popupMenu.add(pasteMenuItem);

        JMenuItem clearMenuItem = new JMenuItem("清除");
        clearMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatArea.setText("");
            }
        });
        popupMenu.add(clearMenuItem);

        popupMenu.show(ChatArea, x, y);
    }
    /**
     * 设置导入按钮
     */
    private void setLoadButton() {
        //Imports user selected file and sets contents to ChatArea
        ImportButton = new JButton("");
        ImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Import prompt");
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String filename = fileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        ChatArea.setText(new String(Files.readAllBytes(Paths.get(filename))));
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        ImportButton.setIcon(new ImageIcon("upFolder.gif"));
        contentPane.add(ImportButton);
    }

    /**
     * 保存按钮
     * Save Button code: takes contents of DisplayArea and saves it in plain text in user selected location with user provided filename
     */
    private void setSaveButton() {
        SaveButton = new JButton("");
        try {
            SaveButton.setIcon(new ImageIcon("FloppyDrive.gif"));
        }catch(Exception e4) {
            JOptionPane.showMessageDialog(null, e4.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        SaveButton.setFont(new Font("Arial Black", Font.BOLD, 6));
        SaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                File defaultDir = new File(".");
                JFileChooser fileChooser = new JFileChooser(defaultDir);
                fileChooser.setDialogTitle("保存聊天记录");

                int result = fileChooser.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {

                    File selectedFile = fileChooser.getSelectedFile();

                    try {

                        FileWriter writer = new FileWriter(selectedFile);
                        String plaintext = DisplayArea.getDocument().getText(0, DisplayArea.getDocument().getLength());
                        writer.write(plaintext);
                        writer.close();
                        JOptionPane.showMessageDialog(null, "聊天文件保存成功");

                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (BadLocationException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });

        contentPane.add(SaveButton);

    }

    /**
     * 创建新页面
     */
    private void setNewChat() {
        ResetButton = new JButton("新建聊天");
        ResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        contentPane.add(ResetButton);
    }

    /**
     * 提交按钮
     */
    private void setSubmitButtom() {

        SubmitButton = new JButton("提交");
        SubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        contentPane.add(SubmitButton);
    }

    /**
     * 提交事件详情
     */
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
                doc.insertString(doc.getLength(),ChatArea.getText() + "\n\n",ChatStyle);
                doc.insertString(doc.getLength(),"ARK",GPTStyle);
                doc.insertString(doc.getLength(),":\n",InvisibleStyle);

                StringBuilder stringBuilder = new StringBuilder();
                //获取user的输入对象,并添加到messages里面
                final ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), ChatArea.getText());
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
                    if (isHTMLView) {
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
                        writeMessagesToFile(FGPTConvo.getPath());
                        if (first&&autotitle) {
                            AutoTitle();
                            first = false;
                        }
                    }
                    //清空当前
                    ChatArea.setText("");
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

    /**
     * 以json格式编写内容
     * @param filename
     */
    public void writeMessagesToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename)) {
            Gson gson = new Gson();
            for (ChatMessage message : messages) {
                String json = gson.toJson(message);
                writer.println(json);
            }
        }
    }

    /**
     * 创建新的文件
     */
    public void newFile() {
        String randfilename = getRandomString();
        FGPTConvo = new File(chatDir + "\\Chat_" + randfilename  + ".json");
        while(FGPTConvo.exists()) {
            randfilename = getRandomString();
            FGPTConvo = new File(chatDir + "\\Chat_" + randfilename + ".json");
        }
        setTitle("JavaGPT - Chat_" + randfilename);
    }

    /**
     * 设置随机名字
     * @return
     */
    public static String getRandomString() {
        String letters = "abcdefghijklmnopqrstuvwxyz1234567890";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int index = rand.nextInt(letters.length());
            sb.append(letters.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 设置菜单栏
     *
     */
    private void setMenu() {
        //初始化菜单栏
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        //选项菜单
        addOptionMenu();
        //删除功能
        addDeleteMenu();
        //回退功能
        addRevert();
        //添加关于
        addAbout();
        //添加历史记录
        addHistory();



        //------------------------------------------------------------------------------------------


    }

    /**
     * 历史消息
     */
    private void addHistory() {
        //Opens "ChatLoader" (Chat History) JFrame
        JMenu LoadChatButton = new JMenu("加载记录");
        LoadChatButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(cloaderopen != true) {
                    cloader = new ChatLoader(chatDir);
                    cloader.setIconImage(Toolkit.getDefaultToolkit().getImage("logo.png"));
                    cloader.setVisible(true);
                    cloaderopen = true;
                    cloader.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            cloaderopen = false;
                        }
                    });
                }
            }
        });

        menuBar.add(LoadChatButton);
    }

    /**
     * 关于消息
     */
    private void addAbout() {

        JMenu aboutMenu = new JMenu("关于");
        aboutMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(aframeopen != true) {
                    //关于信息,新建一个包装类
                    About aframe = new About();
                    //这种方式适用于加载本地文件或远程 URL 中的图像。直接输入url适合在网页里面操作.
                    aframe.setIconImage(Toolkit.getDefaultToolkit().getImage("logo.png"));
                    aframe.setVisible(true);
                    aframeopen = true;
                    aframe.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            aframeopen = false;
                        }
                    });
                }

            }
        });
        menuBar.add(aboutMenu);
    }

    /**
     * 添加回退功能
     */
    private void addRevert() {
        //退回当前对话
        JMenu revert = new JMenu("退后");
        revert.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
    }

    /**
     * 添加删除菜单
     */
    private void addDeleteMenu() {

        //操作上面的文件,用于删除记录
        JMenu delete = new JMenu("删除当前对话");
        delete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (FGPTConvo !=null && FGPTConvo.exists()) {
                    FGPTConvo.delete();
                    reset();
                } else {
                    JOptionPane.showMessageDialog(null, "文件没有找到","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menuBar.add(delete);
    }

    /**
     * 设置两种文本类型
     */
    public static void resetHTMLAreaStyle() {
        HTMLArea.setContentType("text/plain");
        HTMLArea.setContentType("text/html");
    }

    /**
     * 用于重置当前状态
     */
    private void reset() {
        isStreamRunning = false;
        messages.clear();
        FGPTConvo = null;
        GPTConvo = "";
        DisplayArea.setText("");
        HTMLArea.setText("");
        resetHTMLAreaStyle();
        ChatArea.setText("");
        setTitle("JavaGPT");
        first = true;
    }
    /**
     * 添加选项菜单
     */
    private void addOptionMenu() {
        //设置标题
         OptionMenu = new JMenu("选项");
        menuBar.add(OptionMenu);
        //设置页面Menu
        setFormSizeMenu();
        setFontSizeMenu();
        setRenameMenu();
        setViewMenu();

    }

    /**
     * 设置视图方式
     */
    private void setViewMenu() {
        JMenuItem HTMLViewMenuItem = new JMenuItem("HTML View");
        HTMLViewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isHTMLView) {
                    try {
                        scrollPane.setViewportView(DisplayArea);
                        HTMLViewMenuItem.setText("HTML视图");
                        isHTMLView=false;
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }else {
                    try {
                        scrollPane.setViewportView(HTMLArea);
                        resetHTMLAreaStyle();
                        Node document = parser.parse(DisplayArea.getDocument().getText(0, DisplayArea.getDocument().getLength()));
                        HTMLArea.setText(renderer.render(document));
                        HTMLViewMenuItem.setText("正常视图");
                        isHTMLView=true;
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }

            }
        });

        OptionMenu.add(HTMLViewMenuItem);



    }

    /**
     * 当前文件重命名
     */
    private void setRenameMenu() {
        JMenu RenameMenu = new JMenu("重命名");
        OptionMenu.add(RenameMenu);


        //设置自动命名
        JMenuItem AutoMenuItem = new JMenuItem("自动");
        RenameMenu.add(AutoMenuItem);
        AutoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FGPTConvo != null) {
                    AutoTitle();
                }else {
                    JOptionPane.showMessageDialog(null, "No chat file loaded", "Error", JOptionPane.ERROR_MESSAGE);
                }


            }
        });


        //设置自定义命名
        //自定义命名
        JMenuItem custom = new JMenuItem("自定义");
        RenameMenu.add(custom);
        custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FGPTConvo != null) {
                    //代表的是No icon is used.
                    String title = JOptionPane.showInputDialog(null, "请输入标题", "命名", JOptionPane.PLAIN_MESSAGE);
                    if (title!= null) {
                        //以json格式保存,方便将来扩展功能传输信息
                        File file = new File(FGPTConvo.getParentFile(), title + ".json");
                        //判断重名或者存在
                        if (!file.exists()) {
                            //首先重命名
                            FGPTConvo.renameTo(file);
                            //再将对象属性给他
                            FGPTConvo = file;
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
                    File file = new File(FGPTConvo.getParentFile(), title + ".json");
                    if (file.exists()) {
                        JOptionPane.showMessageDialog(null,"文件已存在","Error",JOptionPane.ERROR_MESSAGE);
                        setTitle("ARKGPT - " + FGPTConvo.getName().substring(0,FGPTConvo.getName().length()-5));
                    }
                    else {
                        FGPTConvo.renameTo(file);
                        FGPTConvo = file;
                        INSTANCE.setTitle("CuteGPT - " + FGPTConvo.getName().substring(0,FGPTConvo.getName().length()-5));
                    }
                }
            }
        });
        thread.start();
    }
    /**
     * 设置字体大小
     */
    private void setFontSizeMenu() {

        JMenu FontSizeMenu = new JMenu("字体设置");
        OptionMenu.add(FontSizeMenu);

        JMenuItem DefaultFSMenuItem = new JMenuItem("Default (12)");
        FontSizeMenu.add(DefaultFSMenuItem);
        DefaultFSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FontSize != 12) {
                    FontSize = 12;
                    setFontSize();
                    refreshMessages();
                }
            }
        });

        JMenuItem LargeFSMenuItem = new JMenuItem("Large (16)");
        FontSizeMenu.add(LargeFSMenuItem);
        LargeFSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FontSize != 16) {
                    FontSize = 16;
                    setFontSize();
                    refreshMessages();
                }
            }
        });

        JMenuItem ExtraLargeFSMenuItem = new JMenuItem("Ex-Large (20)");
        FontSizeMenu.add(ExtraLargeFSMenuItem);
        ExtraLargeFSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FontSize != 20) {
                    FontSize = 20;
                    setFontSize();
                    refreshMessages();
                }
            }
        });

        JMenuItem CustomFSMenuItem = new JMenuItem("Custom");
        FontSizeMenu.add(CustomFSMenuItem);
        CustomFSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(null, "Enter font size:", "Font Size", JOptionPane.PLAIN_MESSAGE);
                try {
                    FontSize = Integer.parseInt(input);
                    setFontSize();
                    refreshMessages();
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(null, "Invalid font size", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });


    }

    /**
     * 刷新消息
     */
    public void refreshMessages() {
        DisplayArea.setText("");
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
                    doc.insertString(doc.getLength(), "ARK", GPTStyle);
                    doc.insertString(doc.getLength(), ":\n", InvisibleStyle);
                    doc.insertString(doc.getLength(), message.getContent() + "\n\n", ChatStyle);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 字体设置
     */
    private void setFontSize() {
        StyleConstants.setFontSize(YouStyle, FontSize);
        StyleConstants.setFontSize(GPTStyle, FontSize);
        StyleConstants.setFontSize(ChatStyle, FontSize);
        StyleConstants.setFontSize(ErrorStyle, FontSize);
    }

    /**
     * 设置框的页面大小
     */
    private void setFormSizeMenu() {
        JMenu FormSizeMenu = new JMenu("页面大小");
        OptionMenu.add(FormSizeMenu);


        JMenuItem SmallMenuItem = new JMenuItem("小");
        FormSizeMenu.add(SmallMenuItem);
        SmallMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FormSize != 1) {
                    FormSize = 1;
                    setFormSize();
                }
            }
        });

        JMenuItem MediumMenuItem = new JMenuItem("中");
        FormSizeMenu.add(MediumMenuItem);
        MediumMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FormSize != 2) {
                    FormSize = 2;
                    setFormSize();
                }
            }
        });


        JMenuItem LargeMenuItem = new JMenuItem("大");
        FormSizeMenu.add(LargeMenuItem);
        LargeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(FormSize != 3) {
                    FormSize = 3;
                    setFormSize();
                }
            }
        });
    }

    /**
     * 设置主容器
     */
    private void setMainContentPane() {
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        scrollPane = new JScrollPane();

        contentPane.add(scrollPane);


        DisplayArea = new JEditorPane();
        scrollPane.setViewportView(DisplayArea);
        DisplayArea.setEditable(false);
        DisplayArea.setContentType("text/rtf");

        HTMLArea = new JEditorPane();
        HTMLArea.setEditable(false);
        HTMLArea.setBackground(Color.white);
        HTMLArea.setContentType("text/html");

        //Sets properties for Style objects
        StyleContext sc = StyleContext.getDefaultStyleContext();

        YouStyle = sc.addStyle("bold", null);
        StyleConstants.setForeground(YouStyle, Color.BLACK);
        StyleConstants.setFontFamily(YouStyle, "Tahoma");
        StyleConstants.setFontSize(YouStyle, FontSize);
        StyleConstants.setBold(YouStyle, true);

        GPTStyle = sc.addStyle("bold", null);
        StyleConstants.setFontFamily(GPTStyle, "Tahoma");
        StyleConstants.setFontSize(GPTStyle, FontSize);
        StyleConstants.setBold(GPTStyle, true);
        StyleConstants.setForeground(GPTStyle, Color.RED); //getHSBColor(0, 0.8f, 0.8f)

        InvisibleStyle = sc.addStyle("bold", null);
        StyleConstants.setForeground(InvisibleStyle, DisplayArea.getBackground());

        ChatStyle = sc.addStyle("black", null);
        StyleConstants.setFontFamily(ChatStyle, "Arial");
        StyleConstants.setFontSize(ChatStyle, 25);
        StyleConstants.setForeground(YouStyle, Color.BLACK);

        ErrorStyle = sc.addStyle("ErrorStyle", null);
        StyleConstants.setItalic(ErrorStyle, true);
        StyleConstants.setFontFamily(ErrorStyle, "Tahoma");
        StyleConstants.setFontSize(ErrorStyle, FontSize);

        if(seltheme == 1) {
            StyleConstants.setForeground(YouStyle, Color.ORANGE); //getHSBColor(30f/360, 0.8f, 1f)
            StyleConstants.setForeground(ChatStyle, Color.WHITE); //Color.getHSBColor(0f, 0f, 0.8f)
            StyleConstants.setForeground(ErrorStyle, Color.WHITE); //Color.getHSBColor(0f, 0f, 0.8f)
        }else {
            StyleConstants.setForeground(YouStyle, Color.BLUE);
            StyleConstants.setForeground(ChatStyle, Color.BLACK);
            StyleConstants.setForeground(ErrorStyle, Color.BLACK);
        }
        doc = (StyledDocument) DisplayArea.getDocument();

    }

    /**
     * 设置全局样式
     */
    private static void setFormSize() {
//
        switch (FormSize) {
            case 1:
                // 第一种大小
                frame.getContentPane().setPreferredSize(new Dimension(475, 532));
                scrollPane.setBounds(10, 11, 456, 432);
                scrollPane_1.setBounds(103, 454, 363, 69);
                SubmitButton.setBounds(10, 454, 89, 23);
                SaveButton.setBounds(10, 477, 43, 23);
                ImportButton.setBounds(56, 477, 43, 23);
                ResetButton.setBounds(10, 500, 89, 23);
                break;
            case 2:
                // 第二种大小
                frame.getContentPane().setPreferredSize(new Dimension(1370, 960));
                scrollPane.setBounds(13, 15, 1344, 802);
                scrollPane_1.setBounds(171, 831, 1186, 118);
                SubmitButton.setBounds(13, 831, 148, 36);
                ResetButton.setBounds(13, 914, 148, 36);
                SaveButton.setBounds(13, 873, 73, 36);
                ImportButton.setBounds(88, 873, 73, 36);
                break;
            default:
//                // 默认大小
                frame.getContentPane().setPreferredSize(new Dimension(686, 647));
                scrollPane.setBounds(10, 11, 667, 532);
                scrollPane_1.setBounds(10, 554, 568, 85);
                SubmitButton.setBounds(588, 554, 89, 23);
                ResetButton.setBounds(588, 616, 89, 23);
                SaveButton.setBounds(588, 585, 43, 23);
                ImportButton.setBounds(636, 585, 43, 23);
//                frame.getContentPane().setPreferredSize(new Dimension(686, 647));
//                frame.pack();
//                SubmitButton.setBounds(10, 554, 89, 23);
//                ResetButton.setBounds(10, 616, 89, 23);
//                scrollPane.setBounds(10, 11, 667, 532);
//                scrollPane_1.setBounds(109, 554, 568, 85);
//                SaveButton.setBounds(10, 585, 43, 23);
//                ImportButton.setBounds(56, 585, 43, 23);
                break;
        }

// 调整完样式后调用pack()进行重绘
        frame.pack();


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
                p.put("backgroundPattern", "on");
                //将 Logo 字符串设置为空，可能是指移除了界面上的 Logo 或者设置为空字符串作为占位符
                p.put("logoString", "");
                HiFiLookAndFeel.setCurrentTheme(p);
//                UIManager.setLookAndFeel(new McWinLookAndFeel());
//                UIManager.setLookAndFeel(new MintLookAndFeel());
//                UIManager.setLookAndFeel(new GraphiteLookAndFeel());
//                UIManager.setLookAndFeel(new LunaLookAndFeel());
//                UIManager.setLookAndFeel(new MotifLookAndFeel());
                //UIManager.setLookAndFeel(new AcrylLookAndFeel());
//                UIManager.setLookAndFeel(new AeroLookAndFeel());
//                UIManager.setLookAndFeel(new DarkStarLookAndFeel());
//                UIManager.setLookAndFeel(new GraphiteLookAndFeel());
                /**
                 * 备选
                 */
                UIManager.setLookAndFeel(new NoireLookAndFeel());

//                UIManager.setLookAndFeel(new HiFiLookAndFeel());
                //代表选择了dark
                seltheme = 1;
            }
        }
    }
    /**
     * 设置代理
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

    /**
     * 初始化的时候操作配置文件
     */
    private static void setSettings() {
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
    }

    /**
     * 设置程序的字符编码
     */
    private static void setUniCode() {
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
    }


}


/*
#\u751F\u6210\u914D\u7F6E\u6587\u4EF6
#Thu Jun 01 15:41:10 GMT+08:00 2023
proxytype=https
Theme=dark
chat_location_override=
EnterToSubmit=true
model=gpt-3.5-turbo
apikey=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
proxyip=127.0.0.1
WindowSize=medium
autotitle=true
maxTokens=1024
proxyport=10809
FontSize=12
autoscroll=true
timeout=30
chat_history=true


 */