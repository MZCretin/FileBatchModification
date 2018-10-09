package cretin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sourceforge.pinyin4j.PinyinHelper;

public class Modify extends JFrame {
	private final int mWidth = 600;
	private final int mHeight = 400;
	private JPanel panel;
	private JButton buttonSelect;
	private JButton buttonOk;
	private JCheckBox jCheckBox;
	private JTextField textField;
	private JTextArea textArea;
	private JScrollPane jsp;
	private String currPathString;
	private List<File> list = new ArrayList<File>();
	private String separator;

	public Modify() {
		setTitle("文件批量修改器");
		setSize(mWidth, mHeight);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		buttonSelect = new JButton("选择文件");
		buttonOk = new JButton("开始");
		jCheckBox = new JCheckBox("文件名中文转拼音");
		jCheckBox.setSelected(true);
		textField = new JTextField(10);
		textField.setText("@2x @3x");
		textArea = new JTextArea(20, 48);
		jsp = new JScrollPane(textArea);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		textArea.setLineWrap(true);// 设置文本区的换行策略
		textArea.setEditable(false);

		separator = System.getProperties().getProperty("file.separator");

		panel.add(textField);
		panel.add(jCheckBox);
		panel.add(buttonSelect);
		panel.add(buttonOk);
		panel.add(jsp);

		// 讲述使用说明
		textArea.append(
				"欢迎使用Cretin文件批量修改器 \n1、先在文本框输入不同尺寸图片的后缀(比如：pic@2x.png(两倍图),pic@3x.png(三倍图),那么您应该输入@2x跟@3x)，中间以空格隔开\n");
		textArea.append("2、选择包含所有图片文件的文件夹\n");
		textArea.append("3、点击开始，将为你自动分类图片\n");
		textArea.append("4、图片分类成功后会在你选择的文件夹下面新建以后缀命名的文件夹(比如：@2x和@3两个文件夹，分别装有两倍图和三倍图图片)\n");
		textArea.append("-----------------------------------------\n");

		add(panel);

		// 选择文件夹
		buttonSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tag = textField.getText();
				if (tag == null || tag.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "请在文本框中输入后缀", "系统信息", JOptionPane.WARNING_MESSAGE);
					return;
				}
				textArea.append("输入的后缀为:" + textField.getText() + "\n");
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showDialog(new JLabel(), "选择文件夹");
				File file = jfc.getSelectedFile();
				if (file != null) {
					textArea.append("已选择文件夹：" + file.getAbsolutePath() + "\n");
					currPathString = file.getAbsolutePath();
				} else {
					textArea.append("取消选择文件夹......\n");
				}
			}
		});

		// 开始
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tag = textField.getText();
				if (tag == null || tag.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "请在文本框中输入后缀", "系统信息", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (currPathString == null || currPathString.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "请选择文件夹", "系统信息", JOptionPane.WARNING_MESSAGE);
					return;
				}
				doIt();
			}
		});
		setVisible(true);
	}

	private List<String> fileNameList = new ArrayList<String>();

	/**
	 * 思路 1、遍历所有的文件 2、找出所有以@2和@3结尾的文件 3、将以不同后缀结尾的文件分别复制到各自的文件夹下 4、去掉后缀 5、搞定
	 */
	private void doIt() {
		textArea.append("开始遍历文件....................\n");
		// 所有的文件列表
		list.clear();
		for (File file : list) {
			fileNameList.add(file.getName());
		}
		try {
			showAllFiles(new File(currPathString));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String[] str = textField.getText().split(" ");
		final int count = textField.getText().split(" ").length;
		final List<ArrayList<String>> sourceArrayLists = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < count; i++) {
			sourceArrayLists.add(new ArrayList<String>());
		}
		System.out.println("sourceArrayLists.size() " + sourceArrayLists.size());
		System.out.println("list.size() " + list.size());
		for (int i = 0; i < list.size(); i++) {
			HH: for (int j = 0; j < count; j++) {
				String pathString = list.get(i).getAbsolutePath();
				if (pathString.contains(str[j])) {
					sourceArrayLists.get(j).add(pathString);
					System.out.println(str[j] + "  " + pathString);
					break HH;
				}
			}
		}
		textArea.append("开始复制文件....................\n");
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < count; i++) {
					textArea.append("第" + (i + 1) + "轮复制....................\n");
					fileNameList.clear();
					for (int j = 0; j < sourceArrayLists.get(i).size(); j++) {
						String path = sourceArrayLists.get(i).get(j);
						// banner.png
						String pathAim = path.substring(path.lastIndexOf(separator) + 1).replaceAll(str[i], "");
						if (jCheckBox.isSelected()) {
							String end = pathAim.substring(pathAim.lastIndexOf(".") + 1);
							String start = pathAim.substring(0, pathAim.lastIndexOf("."));
							String resultStart = getPinyi(start);
							String result;
							if (resultStart == null || resultStart.equals("")) {
								// 不含中文 原文输出
								result = pathAim;
							} else {
								result = resultStart + "." + end;
								int index = 0;
								while (fileNameList.contains(result)) {
									// 此次不合格
									result = resultStart + index++ + "." + end;
								}
							}
							fileNameList.add(result);
							System.out.println(path);
							System.out.println(result);
							// C:\Users\sks\Desktop\resources\resources\banner@2x.png
							// C:\Users\sks\Desktop\resources\resources/@2x
							copyFile(path, currPathString + separator + str[i], result);
						} else {
							// C:\Users\sks\Desktop\resources\resources\banner@2x.png
							// C:\Users\sks\Desktop\resources\resources/@2x
							copyFile(path, currPathString + separator + str[i], pathAim);
						}
					}
				}
				textArea.append("********************\n");
				textArea.append("哈哈，文件批量修改完成\n");
				textArea.append("********************\n");
			}
		}).start();
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public synchronized void copyFile(String oldPath, String newPath, String fileName) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				newFile.mkdir();
			}
			File tempFile = new File(newFile.getAbsoluteFile(), fileName);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(tempFile.getAbsoluteFile());
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				textArea.append(oldPath + "->" + fileName + "复制成功\n");
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}

	private String getNewPath(String oldPath, String split) {
		String fileName = oldPath.substring(oldPath.lastIndexOf(separator));
		return currPathString + File.separator + split + fileName.replaceAll(split, "");
	}

	private void showAllFiles(File dir) throws Exception {
		File[] fs = dir.listFiles();
		if (fs == null) {
			JOptionPane.showMessageDialog(getContentPane(), "文件夹内无数据\n" + dir.getPath(), "系统信息",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isDirectory()) {
				textArea.append("文件夹:" + fs[i].getAbsolutePath() + "\n");
				try {
					showAllFiles(fs[i]);
				} catch (Exception e) {
				}
			} else {
				textArea.append("文件:" + fs[i].getAbsolutePath() + "\n");
				list.add(fs[i]);
			}
		}
	}

	// 程序入口
	public static void main(String[] args) {
		new Modify();
	}

	/**
	 * 获取文字的拼音组合
	 * 
	 * @param text
	 * @return
	 */
	public static String getPinyi(String text) {
		if (text.length() > 4) {
			text = text.substring(0, 4);
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(text.charAt(i));
			if (pinyinArray != null && pinyinArray.length > 0) {
				String str = pinyinArray[0];
				if (i < 2) {
					stringBuffer.append(str.substring(0, str.length() - 1));
				} else {
					if (str.length() > 0)
						stringBuffer.append(str.substring(0, 1));
				}
			}
		}
		return stringBuffer.toString();
	}
}
