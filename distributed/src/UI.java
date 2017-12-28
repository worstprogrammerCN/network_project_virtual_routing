import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import Utils.MsgPacket;

public class UI {
	public JTextField localHost;
	public JButton sendDistance = new JButton("连接");
	public JButton sendMessage = new JButton("发送");
	public JTextField neighbour = new HintTextField("邻居IP地址");
	public JTextField neighbourDis = new HintTextField("距离");
	public JTextField otherHost = new HintTextField("其他主机IP");
	public JTextField message = new HintTextField("发送消息");
	public JFrame frame = new JFrame();
	public JTextArea logInfomation = new JTextArea("连接状态log:\n");
	public JTextArea routeInformation = new JTextArea("路由信息log:\n");
	public JPanel jPanel = new JPanel();
	public GridBagLayout gridBagLayout = new GridBagLayout();
	public Host host;
	
	public UI(Host _host) {
		host = _host;
		//得到IP
		localHost = new HintTextField(host.localIP);
		go();
	}

	public void setTextFieldStyle(JTextField textField, GridBagConstraints constraints, int gridwidth, int weightx, int weighty) {
		textField.setFont(new Font("Microsoft Yahei", Font.ITALIC, 18));
		textField.setBorder(BorderFactory.createLineBorder(new Color(195, 211,
                230), 1));
		constraints.gridwidth = gridwidth;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		gridBagLayout.setConstraints(textField, constraints);
		jPanel.add(textField);
	}

	public void setButtonStyle(JButton button, GridBagConstraints constraints, int gridwidth, int weightx, int weighty) {
		button.setFont(new Font("Microsoft Yahei", Font.BOLD, 18));
		button.setBorder(BorderFactory.createLineBorder(new Color(195, 211,
                230), 1));
		constraints.gridwidth = gridwidth;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		gridBagLayout.setConstraints(button, constraints);
		jPanel.add(button);
	}

	public void setTextAreaStyle(JTextArea textArea, GridBagConstraints constraints, int gridwidth, int weightx, int weighty, int flag) {
		if (flag == 1) {
			textArea.setLineWrap(true);
			textArea.setFont(new Font("Microsoft Yahei", Font.PLAIN, 16));
			textArea.setWrapStyleWord(true);
			JScrollPane scroll = new JScrollPane(textArea);
			scroll.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			constraints.weighty = weighty;
			constraints.weightx = weightx;
			constraints.gridwidth = gridwidth;
			gridBagLayout.setConstraints(scroll, constraints);

			jPanel.add(scroll);
		} else {
			textArea.setLineWrap(true);
			textArea.setFont(new Font("Microsoft Yahei", Font.PLAIN, 16));
			textArea.setWrapStyleWord(true);

			constraints.weighty = weighty;
			constraints.weightx = weightx;
			constraints.gridwidth = gridwidth;
			gridBagLayout.setConstraints(textArea, constraints);

			jPanel.add(textArea);
		}
	}

	/**
	 * 添加UI部件，容器:GridBagLayout
	 *
	 * @param
	 */
	public void go() {
		//设置panel样式
		jPanel.setVisible(true);
		jPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		jPanel.setLayout(gridBagLayout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		//设置每一个部件的样式(宽度、边框、字体等)
		localHost.setEnabled(false);
		setTextFieldStyle(localHost, constraints, GridBagConstraints.REMAINDER, 0, 0);

		JTextArea blank = new JTextArea("");
		blank.setEnabled(false);
		setTextAreaStyle(blank, constraints, GridBagConstraints.REMAINDER, 0, 0, 0);

		setTextFieldStyle(neighbour, constraints, 1, 2, 0);
		setTextFieldStyle(neighbourDis, constraints, 2, 1, 0);
		setButtonStyle(sendDistance, constraints, GridBagConstraints.REMAINDER, 0, 0);

		JTextArea blank1 = new JTextArea("");
		blank1.setEnabled(false);
		setTextAreaStyle(blank1, constraints, GridBagConstraints.REMAINDER, 0, 0, 0);
		
		setTextFieldStyle(otherHost, constraints, 1, 2, 0);
		setTextFieldStyle(message, constraints, 2, 1, 0);
		setButtonStyle(sendMessage, constraints, GridBagConstraints.REMAINDER, 0, 0);

		setTextAreaStyle(logInfomation, constraints, GridBagConstraints.REMAINDER, 1, 1, 1);
		setTextAreaStyle(routeInformation, constraints, GridBagConstraints.REMAINDER, 1, 1, 1);
		frame.add(jPanel);

		//设置frame样式(适配屏幕、标题等)
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int)(dimension.width * 0.3), (int)(dimension.height * 0.9));
		frame.setLocation((int)(dimension.width*0.35), 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("IP路由");

		sendDistance.addActionListener(new BtnListen1());
		sendMessage.addActionListener(new BtnListen2());


		frame.setVisible(true);
	}

	public void appendLogInfo(String info) {
		String original = logInfomation.getText();
		logInfomation.setText(original + "\n" + info + "\n");
	}

	public void appendRouteInfo(String info) {
		String original = routeInformation.getText();
		routeInformation.setText(original + "\n" + info + "\n");
	}


	/**
	 * 添加与邻居Host连接的监听器
	 */
	class BtnListen1 implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String IP = neighbour.getText();
			String _distance = neighbourDis.getText();
			if (neighbour.getText().equals("")
					|| neighbourDis.getText().equals("")) {
				JOptionPane.showMessageDialog(null,
						"not input number to perfrom!");
			} else {
				try {
					int dis = Integer.parseInt(_distance);
					host.createConnet(IP, dis);
					// JOptionPane.showMessageDialog(null,
					// "Your have not decided which operation to perform!");
				} catch (NumberFormatException exception) {
					JOptionPane.showMessageDialog(null,
							"The Input is not a number!");
				}
			}
		}
	}

	/**
	 * 添加于某一Host通讯监听器
	 */
	class BtnListen2 implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String targetIP = otherHost.getText();
			String msg = message.getText();
			if (targetIP.equals("") || msg.equals("")) {
				JOptionPane.showMessageDialog(null,
						"not input number to perfrom!");
			} else {
				MsgPacket msgPacket = new MsgPacket(msg, 1, host.localIP, targetIP);
				host.sendMessagePacket(msgPacket);
			}
		}
	}

	/**
	 * 设置主机IP
	 *
	 * @param IP:用户输入主机地址
	 */
	public void setLocalIP(String IP) {
		localHost.setText(IP);
	}
}

class HintTextField extends JTextField implements FocusListener {
	private final String hint;
	private boolean showingHint;

	/**
	 * 新建带有提示信息的textField
	 *
	 * @param hint:提示信息
	 */
	public HintTextField(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	/**
	 * 监听鼠标位置
	 *
	 * @param e:鼠标焦点获得事件
	 */
	@Override
	public void focusGained(FocusEvent e) {
		if(this.getText().isEmpty()) {
			super.setText("");
			showingHint = false;
		}
	}

	/**
	 * 监听鼠标位置
	 *
	 * @param e:鼠标焦点失去时间
	 */
	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}

	/**
	 * 获取textField内容
	 */
	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
}
