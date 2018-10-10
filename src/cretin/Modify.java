package cretin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cretin.listener.JTextFieldHintListener;
import cretin.model.TranslateModel;
import net.sourceforge.pinyin4j.PinyinHelper;
import translate.TransApi;

public class Modify extends JFrame {
	private final int mWidth = 600;
	private final int mHeight = 430;
	private static final String APP_ID = "20181010000217324";
	private static final String SECURITY_KEY = "DmBJdQAnrfIEwgAfFKgN";
	private JPanel panel;
	private JButton buttonSelect;
	private JButton buttonOk;
	private JCheckBox jCheckBox0;
	private JCheckBox jCheckBox;
	private JCheckBox jCheckBox1;
	private JCheckBox jCheckBox2;
	private JTextField textField;
	private JTextArea textArea;
	private JComboBox jcb;
	private JLabel jLabel;
	private JScrollPane jsp;
	private String currPathString;
	private List<File> list = new ArrayList<File>();
	private String separator;
	private Gson gson;
	private TransApi api;

	private String jcbList[] = { "不限", "5", "8", "11", "14", "17" };

	public Modify() {
		gson = new Gson();
		setTitle("文件批量修改器");
		setSize(mWidth, mHeight);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		buttonSelect = new JButton("选择文件");
		buttonOk = new JButton("开始");
		jCheckBox0 = new JCheckBox("保留原文件名");
		jCheckBox = new JCheckBox("文件名中文转拼音");
		jCheckBox1 = new JCheckBox("文件名中文转英文");
		jCheckBox2 = new JCheckBox("仅翻译文件名");
		jLabel = new JLabel("设置文件名称最大长度");
		jcb = new JComboBox(jcbList);
		jCheckBox1.setSelected(true);
		textField = new JTextField(10);
		textField.setText("@2x @3x");
		textField.addFocusListener(new JTextFieldHintListener("请输入尺寸后缀", textField));
		textArea = new JTextArea(20, 48);
		jsp = new JScrollPane(textArea);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		textArea.setLineWrap(true);// 设置文本区的换行策略
		textArea.setEditable(false);

		separator = System.getProperties().getProperty("file.separator");

		panel.add(textField);
		panel.add(jCheckBox0);
		panel.add(jCheckBox);
		panel.add(jCheckBox1);
		panel.add(jCheckBox2);
		panel.add(jLabel);
		panel.add(jcb);
		panel.add(buttonSelect);
		panel.add(buttonOk);
		panel.add(jsp);

		// 讲述使用说明
		textArea.append(
				"欢迎使用Cretin文件批量修改器 \n1、先在文本框输入不同尺寸图片的后缀(比如：pic@2x.png(两倍图),pic@3x.png(三倍图),那么您应该输入@2x跟@3x)，中间以空格隔开\n");
		textArea.append("2、选择包含所有图片文件的文件夹\n");
		textArea.append("3、点击开始，将为你自动分类图片\n");
		textArea.append(
				"4、图片分类成功后会在你选择的文件夹下面新建以后缀命名的文件夹(比如：@2x和@3两个文件夹，分别装有两倍图和三倍图图片)\n5、使用中有什么问题可联系：mxnzp_life@163.com\n");
		textArea.append("-----------------------------------------\n");

		add(panel);

		jCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JCheckBox jcb = (JCheckBox) e.getItem();// 将得到的事件强制转化为JCheckBox类
				if (jcb.isSelected()) {// 判断是否被选择
					jCheckBox0.setSelected(false);
					jCheckBox1.setSelected(false);
				}
			}
		});

		jCheckBox0.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JCheckBox jcb = (JCheckBox) e.getItem();// 将得到的事件强制转化为JCheckBox类
				if (jcb.isSelected()) {// 判断是否被选择
					jCheckBox.setSelected(false);
					jCheckBox1.setSelected(false);
				}
			}
		});

		jCheckBox1.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JCheckBox jcb = (JCheckBox) e.getItem();// 将得到的事件强制转化为JCheckBox类
				if (jcb.isSelected()) {// 判断是否被选择
					jCheckBox0.setSelected(false);
					jCheckBox.setSelected(false);
				}
			}
		});

		// 选择文件夹
		buttonSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jCheckBox2.isSelected()) {
					// 仅翻译文件名
					textArea.append("当前模式:仅修改文件名" + "\n");
				} else {
					String tag = textField.getText();
					if (tag == null || tag.equals("")) {
						JOptionPane.showMessageDialog(getContentPane(), "请在文本框中输入后缀", "系统信息",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					textArea.append("当前模式:根据后缀区分并命名\n输入的后缀为:" + textField.getText() + "\n");
				}

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
				if (jCheckBox2.isSelected()) {
					// 仅翻译文件名
				} else {
					String tag = textField.getText();
					if (tag == null || tag.equals("")) {
						JOptionPane.showMessageDialog(getContentPane(), "请在文本框中输入后缀", "系统信息",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
				if (currPathString == null || currPathString.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "请选择文件夹", "系统信息", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (!jCheckBox.isSelected() && !jCheckBox0.isSelected() && !jCheckBox1.isSelected()) {
					JOptionPane.showMessageDialog(getContentPane(), "请选择处理文件名的模式", "系统信息", JOptionPane.WARNING_MESSAGE);
					return;
				}
				doIt();
			}
		});
		setVisible(true);

		api = new TransApi(APP_ID, SECURITY_KEY);
	}

	private List<String> fileNameList = new ArrayList<String>();
	private int count = 0;

	/**
	 * 思路 1、遍历所有的文件 2、找出所有以@2和@3结尾的文件 3、将以不同后缀结尾的文件分别复制到各自的文件夹下 4、去掉后缀 5、搞定
	 */
	private void doIt() {
		textArea.append("开始遍历文件....................\n");
		// 所有的文件列表
		list.clear();
		// 清除所有缓存
		keys.clear();
		try {
			showAllFiles(new File(currPathString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		count = 0;
		final String[] str = textField.getText().split(" ");
		if (jCheckBox2.isSelected()) {
			count = 1;
		} else {
			count = textField.getText().split(" ").length;
		}
		final List<ArrayList<String>> sourceArrayLists = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < count; i++) {
			sourceArrayLists.add(new ArrayList<String>());
		}
		System.out.println("sourceArrayLists.size() " + sourceArrayLists.size());
		System.out.println("list.size() " + list.size());
		for (int i = 0; i < list.size(); i++) {
			if (jCheckBox2.isSelected()) {
				// 仅修改文件
				String pathString = list.get(i).getAbsolutePath();
				sourceArrayLists.get(0).add(pathString);
				System.out.println("翻译" + "  " + pathString);
			} else {
				HH: for (int j = 0; j < count; j++) {
					String pathString = list.get(i).getAbsolutePath();
					if (pathString.contains(str[j])) {
						sourceArrayLists.get(j).add(pathString);
						System.out.println(str[j] + "  " + pathString);
						break HH;
					}
				}
			}
		}
		textArea.append("开始复制文件....................\n");
		new Thread(new Runnable() {
			public void run() {
				int length = 100000;
				try {
					length = Integer.parseInt(jcb.getSelectedItem().toString());
				} catch (Exception e) {
					// TODO: handle exception
				}
				for (int i = 0; i < count; i++) {
					textArea.append("第" + (i + 1) + "轮复制....................\n");
					fileNameList.clear();
					for (int j = 0; j < sourceArrayLists.get(i).size(); j++) {
						String path = sourceArrayLists.get(i).get(j);
						// banner.png
						String pathAim = path.substring(path.lastIndexOf(separator) + 1).replaceAll(str[i], "");
						if (jCheckBox0.isSelected()) {
							// 保留原文件名
							String end = pathAim.substring(pathAim.lastIndexOf(".") + 1);
							String start = pathAim.substring(0, pathAim.lastIndexOf("."));
							System.out.println(pathAim + "->" + start);
							String result;
							if (length < start.length()) {
								// 长度超了
								start = start.substring(0, length);
							}
							result = start + "." + end;
							int index = 0;
							while (fileNameList.contains(result)) {
								// 此次不合格
								result = start.substring(0, start.length() - (index + "").length()) + index++ + "."
										+ end;
							}
							System.out.println(path);
							System.out.println(result);
							// C:\Users\sks\Desktop\resources\resources\banner@2x.png
							// C:\Users\sks\Desktop\resources\resources/@2x
							if (jCheckBox2.isSelected()) {
								// 仅翻译
								copyFile(path, currPathString + separator + "翻译文件名", result);
							} else
								copyFile(path, currPathString + separator + str[i], result);
						} else if (jCheckBox.isSelected()) {
							// 中文转拼音
							String end = pathAim.substring(pathAim.lastIndexOf(".") + 1);
							String start = pathAim.substring(0, pathAim.lastIndexOf("."));
							String resultStart = getPinyi(start);
							System.out.println(pathAim + "->" + resultStart);
							String result;
							if (resultStart == null || resultStart.equals("")) {
								// 不含中文 原文输出
								if (length < start.length()) {
									// 长度超了
									start = start.substring(0, length);
								}
								result = start + "." + end;
								int index = 0;
								while (fileNameList.contains(result)) {
									// 此次不合格
									result = start.substring(0, start.length() - (index + "").length()) + index++ + "."
											+ end;
								}
							} else {
								if (length < resultStart.length()) {
									// 长度超了
									resultStart = resultStart.substring(0, length);
								}
								result = resultStart + "." + end;
								int index = 0;
								while (fileNameList.contains(result)) {
									// 此次不合格
									result = resultStart.substring(0, resultStart.length() - (index + "").length())
											+ index++ + "." + end;
								}
							}
							fileNameList.add(result);
							System.out.println(path);
							System.out.println(result);
							// C:\Users\sks\Desktop\resources\resources\banner@2x.png
							// C:\Users\sks\Desktop\resources\resources/@2x
							if (jCheckBox2.isSelected()) {
								// 仅翻译
								copyFile(path, currPathString + separator + "翻译文件名", result);
							} else
								copyFile(path, currPathString + separator + str[i], result);
						} else if (jCheckBox1.isSelected()) {
							// 用英文翻译
							String end = pathAim.substring(pathAim.lastIndexOf(".") + 1);
							String start = pathAim.substring(0, pathAim.lastIndexOf("."));
							// banner.png banner
							if (keys.containsKey(start)) {
								start = keys.get(start);
							} else
								start = dealTranslate(start);
							System.out.println(pathAim + "->" + start);
							String result;
							if (length < start.length()) {
								// 长度超了
								start = start.substring(0, length);
							}
							result = start + "." + end;
							int index = 0;
							while (fileNameList.contains(result)) {
								// 此次不合格
								result = start.substring(0, start.length() - (index + "").length()) + index++ + "."
										+ end;
							}
							fileNameList.add(result);
							System.out.println(path);
							System.out.println(result);
							// C:\Users\sks\Desktop\resources\resources\banner@2x.png
							// C:\Users\sks\Desktop\resources\resources/@2x
							if (jCheckBox2.isSelected()) {
								// 仅翻译
								copyFile(path, currPathString + separator + "翻译文件名", result);
							} else
								copyFile(path, currPathString + separator + str[i], result);
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
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public synchronized void copyFile(String oldPath, String newPath, String fileName) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				newFile.mkdir();
			}
			File tempFile = new File(newFile.getAbsoluteFile(), fileName);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldPath); // 读入原文件
				fs = new FileOutputStream(tempFile.getAbsoluteFile());
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				textArea.append(oldPath + "->" + fileName + " 复制成功\n");
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null)
					inStream.close();
				if (fs != null)
					fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	// 中文验证规则
	private String regEx = "[\u4e00-\u9fa5]{1,}";
	// 编译正则表达式
	private Pattern pattern = Pattern.compile(regEx);

	private Map<String, String> keys = new HashMap<String, String>();

	/**
	 * 对翻译结果进行处理
	 * 
	 * @param res
	 * @return
	 */
	public String dealTranslate(String res) {
		Matcher matcher = pattern.matcher(res);
		// 字符串是否与正则表达式相匹配
		StringBuffer finalStr = new StringBuffer();
		int end = 0;
		while (matcher.find()) {
			finalStr.append(res.substring(end, matcher.start()));
			String result = translate(matcher.group());
			finalStr.append(result);
			end = matcher.end();
		}
		finalStr.append(res.substring(end, res.length()));
		String result = finalStr.toString().replaceAll(" ", "_");
		keys.put(res, result);
		return result;
	}

	/**
	 * 翻译
	 * 
	 * @param res
	 * @return
	 */
	private String translate(String res) {
		String result = api.getTransResult(res, "zh", "en");
		TranslateModel translateModel = gson.fromJson(result, new TypeToken<TranslateModel>() {
		}.getType());
		if (translateModel.getTrans_result() != null && !translateModel.getTrans_result().isEmpty()) {
			String translate = translateModel.getTrans_result().get(0).getDst().toLowerCase();
			return translate;
		}
		return "";
	}

	/**
	 * 获取文字的拼音组合
	 * 
	 * @param text
	 * @return
	 */
	public static String getPinyi(String text) {
		// if (text.length() > 4) {
		// text = text.substring(0, 4);
		// }
		text = text.replaceAll(" ", "");
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(text.charAt(i));
			if (pinyinArray != null && pinyinArray.length > 0) {
				String str = pinyinArray[0];
				stringBuffer.append(str.substring(0, str.length() - 1));
			}
		}
		return stringBuffer.toString();
	}
}
