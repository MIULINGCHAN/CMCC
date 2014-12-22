package com.ggsn.client;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.ocs.utils.TimeUtils;

public class SimulatorGUI {
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 500;

	private JFrame frame;
	private JTextField meanInput;
	private JTextField standardDeviationInput;
	private JTextField maxValueInput;
	private JTextField dataNumsInput;
	private JTextField timeIntervalInput;
	private JTextArea showResultArea;

	private GGSN ggsn = null;

	public static void main(String[] args) {
		SimulatorGUI simulatorGUI = new SimulatorGUI();
		simulatorGUI.initGGSNSimulator();
	}

	public void initGGSNSimulator() {
		initGUI();
		ggsn = new GGSN();
	}

	public GGSN getGgsn() {
		return ggsn;
	}

	public void setGgsn(GGSN ggsn) {
		this.ggsn = ggsn;
	}

	public void initGUI() {
		frame = new JFrame("网元网关模拟器");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(
				new FlowLayout(FlowLayout.CENTER, 50, 30));

		// 用户输入区域
		JPanel leftPanel = new JPanel();

		meanInput = new JTextField(10);
		standardDeviationInput = new JTextField(10);
		maxValueInput = new JTextField(10);
		dataNumsInput = new JTextField(10);
		timeIntervalInput = new JTextField(10);

		JLabel meanLable = new JLabel("期望(MM:HH:ss)：");
		JLabel standardDeviationLabel = new JLabel("标准差(s)：");
		JLabel maxValueLabel = new JLabel("峰值：");
		JLabel dataNumsLabel = new JLabel("生成数据个数：");
		JLabel timeIntervalLabel = new JLabel("发送时间间隔(s)：");

		GridLayout grid = new GridLayout(5, 2);
		grid.setVgap(15);
		leftPanel.add(meanLable);
		leftPanel.add(meanInput);
		leftPanel.add(standardDeviationLabel);
		leftPanel.add(standardDeviationInput);
		leftPanel.add(maxValueLabel);
		leftPanel.add(maxValueInput);
		leftPanel.add(dataNumsLabel);
		leftPanel.add(dataNumsInput);
		leftPanel.add(timeIntervalLabel);
		leftPanel.add(timeIntervalInput);
		leftPanel.setLayout(grid);
		frame.getContentPane().add(leftPanel);

		// 中间区域功能按钮
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(3, 1, 5, 80));
		JButton resetButton = new JButton("置空");
		JButton startButton = new JButton("开始仿真");
		JButton exitButton = new JButton("退出仿真");
		resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		resetButton.addActionListener(new ResetButtonListener());// 添加监听器
		startButton.addActionListener(new StartButtonListener()); // 添加监听器
		exitButton.addActionListener(new ExitButtonListener()); // 添加监听器
		buttonsPanel.add(resetButton);
		buttonsPanel.add(startButton);
		buttonsPanel.add(exitButton);
		frame.getContentPane().add(buttonsPanel);

		// 显示计算结果区域
		JPanel resultPanel = new JPanel();

		JPanel resultTitlePanel = new JPanel();
		resultTitlePanel.add(new JLabel("仿真过程输出"));

		showResultArea = new JTextArea(20, 40);
		JScrollPane showResultAreaScroller = new JScrollPane(showResultArea);
		showResultAreaScroller
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		showResultAreaScroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		resultPanel.add(resultTitlePanel);
		resultPanel.add(showResultAreaScroller);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		frame.getContentPane().add(resultPanel);

		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);
	}

	/**
	 * 内部监听器类，用于监听“重置”按钮
	 * 
	 * @author WangChao
	 */
	class ResetButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			int response = JOptionPane.showConfirmDialog(frame,
					"置空将清空之前输入的所有数据！", "提示", JOptionPane.YES_NO_OPTION);

			if (response == 0) {
				meanInput.setText("");
				standardDeviationInput.setText("");
				maxValueInput.setText("");
				dataNumsInput.setText("");
				timeIntervalInput.setText("");
				showResultArea.setText("");
			}
		}
	}

	/**
	 * 内部监听器类，用于监听“计算”按钮
	 * 
	 * @author WangChao
	 */
	class StartButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String mean = meanInput.getText().trim();
			String sd = standardDeviationInput.getText().trim();
			String maxValue = maxValueInput.getText().trim();
			String dataNums = dataNumsInput.getText().trim();
			final String timeInterval = timeIntervalInput.getText().trim();
			
			final List<Integer> simulationData = SimulationDataGenerator.generateNormalSimulationData(
					TimeUtils.convertTimeToInt(mean), Integer.parseInt(sd),
					Integer.parseInt(maxValue), Integer.parseInt(dataNums));

			if (mean.equals("") || sd.equals("") || maxValue.equals("") 
					|| dataNums.equals("") || timeInterval.equals("")) {
				JOptionPane.showMessageDialog(frame, "未输入任何数据！", "错误提示",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			//单独开启一个线程用于生成用户连接，避免阻塞GUI主线程
			try {
				Thread generateConnectionThread = new Thread() {
					public void run() {
						try {
							ggsn.runGGSN(simulationData, Integer.parseInt(timeInterval), showResultArea);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				generateConnectionThread.start();
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 内部监听器类，用于监听“退出”按钮
	 * 
	 * @author WangChao
	 * 
	 */
	class ExitButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			int response = JOptionPane.showConfirmDialog(frame, "确认退出？", "提示",
					JOptionPane.YES_NO_OPTION);

			if (response == 0)
				System.exit(0);
		}
	}
}
