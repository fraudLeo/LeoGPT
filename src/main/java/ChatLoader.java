import vo.FileListItem;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import java.awt.BorderLayout;
public class ChatLoader extends JFrame{
    private JPanel mainP;
    private DefaultListModel<FileListItem> fileListItemDefaultListModel;
    JList<FileListItem> jList;
    private JPopupMenu menu1;
    private JPopupMenu menu2;
    public ChatLoader(String path) {
        setTitle("Chat历史");
        /**
         * 只能关闭当前窗口,不能关闭整个程序的JVM
         */
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100,100,450,300);
        JPanel jPanel = new JPanel();
        //设置这个页面上下左右的边框都是5个单位
        jPanel.setBorder(new EmptyBorder(5,5,5,5));
        //添加jPanel组件
        setContentPane(jPanel);

        //可以使用这个泛型列表模型来管理文件列表的数据，
        //并使用适当的方法添加、删除、访问和操作文件列表中的元素
        fileListItemDefaultListModel = new DefaultListModel<FileListItem>();
        jList = new JList<>(fileListItemDefaultListModel);

        //设置弹出窗口
        menu1 = new JPopupMenu();
        menu2 = new JPopupMenu();

        //准备添加菜单项目
        JMenuItem deleteItem = new JMenuItem("删除");
        JMenuItem refreshItem = new JMenuItem("刷新");
        JMenuItem renameItem = new JMenuItem("重命名");
        JMenuItem sortItem = new JMenuItem("排序");

        JMenuItem refreshItem2 = new JMenuItem("刷新");
        JMenuItem sortItem2 = new JMenuItem("排序");

        menu1.add(deleteItem);
        menu1.add(refreshItem);
        menu1.add(renameItem);
        menu1.add(sortItem);

        menu2.add(refreshItem2);
        menu2.add(sortItem2);

        //添加删除按钮的事件监听
        deleteItem.addActionListener(e-> {
            //判断点击的窗口是否存在对象存疑
            int selectedIndex = jList.getSelectedIndex();
            if (selectedIndex != -1) {
                String filePath = jList.getModel().getElementAt(selectedIndex).getFilePath();
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                    fileListItemDefaultListModel.removeElementAt(selectedIndex);
                } else {
                    JOptionPane.showMessageDialog(null, "没有找到文件", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //还有几个按钮的监听事件等会儿添加
    }
}
