package leo.gptSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class ChatLoader extends JFrame {
    private JPanel contentPane;
    private JList<FileListItem> fileList;
    private DefaultListModel<FileListItem> model;
    private JPopupMenu popupMenu;
    private JPopupMenu popupMenu2;
    private String path;
    private int selectedIndex;

    /**
     * 测试运行
     */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatLoader frame = new ChatLoader();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

    class FileListItem {
        private String displayName;
        private String filePath;

        public FileListItem(String displayName, String filePath) {
            this.displayName = displayName;
            this.filePath = filePath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFilePath() {
            return filePath;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Create the frame.
     */
    public ChatLoader(String path) {
        this.path = path;
        setTitle("Chat History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 288);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        model = new DefaultListModel<>();
        fileList = new JList<>(model);


        popupMenu = new JPopupMenu();
        popupMenu2 = new JPopupMenu();

        // 添加菜单
        JMenuItem deleteItem = new JMenuItem("删除");
        JMenuItem renameItem = new JMenuItem("重命名");
        JMenuItem refreshItems = new JMenuItem("刷新");
        JMenuItem sortItems = new JMenuItem("排序");

        JMenuItem refreshItems2 = new JMenuItem("刷新");
        JMenuItem sortItems2 = new JMenuItem("排序");

        popupMenu.add(deleteItem);
        popupMenu.add(renameItem);
        popupMenu.add(refreshItems);
        popupMenu.add(sortItems);

        popupMenu2.add(refreshItems2);
        popupMenu2.add(sortItems2);


        //如果存在文件，就进行删除
        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                File file = new File(fileList.getModel().getElementAt(selectedIndex).filePath); //替换路径
                if(file.exists()) { //检查文件是否存在
                    file.delete(); //如果文件存在，就进行删除
                    model.removeElementAt(selectedIndex);
                } else {
                    JOptionPane.showMessageDialog(null, "没有找到文件", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });


        //重命名功能
        renameItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String title = JOptionPane.showInputDialog(null, "请输入标题:", "Rename", JOptionPane.PLAIN_MESSAGE);
                if (title != null) {
                    File file = new File(fileList.getModel().getElementAt(selectedIndex).filePath);
                    String path = file.getParent();
                    String name = file.getName();
                    String ext = name.substring(name.lastIndexOf('.'));
                    File newFile = new File(path, title + ext);

                    if (newFile.exists()) {
                        JOptionPane.showMessageDialog(null, "文件已经存在了", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        File txtFile = new File(path, title + ".json");
                        file.renameTo(newFile);
                        new File(path, name.substring(0, name.length() - ext.length()) + ".json").renameTo(txtFile);
                        refreshlist();
                        JOptionPane.showMessageDialog(null, "文件重命名成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            }
        });

        //对弹出窗口进行监听
        refreshItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshlist();
            }
        });

        refreshItems2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshlist();
            }
        });

        sortItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ARKFrame.isAlpha = !ARKFrame.isAlpha;
                refreshlist();
            }
        });

        sortItems2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ARKFrame.isAlpha = !ARKFrame.isAlpha;
                refreshlist();
            }
        });
        //------------------------------------------

        //将程序加载到主页面的主程序中
        fileList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e);
            }
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectedIndex = fileList.getSelectedIndex();
                    try {
                        ARKFrame.loadchat(fileList.getModel().getElementAt(selectedIndex).filePath, fileList.getModel().getElementAt(selectedIndex).displayName);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            }
        });

        refreshlist();
        contentPane.setLayout(new BorderLayout(0, 0));
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setViewportView(fileList);
        contentPane.add(scrollPane);
    }

//    使用目录中的文件名刷新列表。
//    目录由“路径”变量表示。
//    该函数应用一个文件过滤器，该过滤器仅接受扩展名为“.json”的文件，并根据“isAlpha”静态布尔变量的值按上次修改日期或字母顺序对它们进行排序。
//    最后，它为每个文件创建一个新列表项，其中包含其名称和完整路径（不带扩展名），并将其添加到列表模型中。
    public void refreshlist() {
        File directory = new File(path);
        model.clear();
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".json");
            }
        });

        if(ARKFrame.isAlpha) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f2.lastModified() - f1.lastModified();
                    return Long.signum(diff);
                }
            });
        }

        for (File file : files) {
            String displayName = file.getName();
            String filePath = file.getAbsolutePath();
            FileListItem item = new FileListItem(displayName.replaceFirst("[.][^.]+$", ""), filePath);
            model.addElement(item);
        }
    }

    //根据是否选择了文件列表中的文件，在右键单击时显示正确的弹出菜单
    private void showPopupMenu(MouseEvent e) {
        selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex == -1) {
            popupMenu2.show(e.getComponent(), e.getX(), e.getY());
        }else {

            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
