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
		frame = new JFrame("��Ԫ����ģ����");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(
				new FlowLayout(FlowLayout.CENTER, 50, 30));

		// �û���������
		JPanel leftPanel = new JPanel();

		meanInput = new JTextField(10);
		standardDeviationInput = new JTextField(10);
		maxValueInput = new JTextField(10);
		dataNumsInput = new JTextField(10);
		timeIntervalInput = new JTextField(10);

		JLabel meanLable = new JLabel("����(MM:HH:ss)��");
		JLabel standardDeviationLabel = new JLabel("��׼��(s)��");
		JLabel maxValueLabel = new JLabel("��ֵ��");
		JLabel dataNumsLabel = new JLabel("�������ݸ�����");
		JLabel timeIntervalLabel = new JLabel("����ʱ����(s)��");

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

		// �м������ܰ�ť
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(3, 1, 5, 80));
		JButton resetButton = new JButton("�ÿ�");
		JButton startButton = new JButton("��ʼ����");
		JButton exitButton = new JButton("�˳�����");
		resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		resetButton.addActionListener(new ResetButtonListener());// ��Ӽ�����
		startButton.addActionListener(new StartButtonListener()); // ��Ӽ�����
		exitButton.addActionListener(new ExitButtonListener()); // ��Ӽ�����
		buttonsPanel.add(resetButton);
		buttonsPanel.add(startButton);
		buttonsPanel.add(exitButton);
		frame.getContentPane().add(buttonsPanel);

		// ��ʾ����������
		JPanel resultPanel = new JPanel();

		JPanel resultTitlePanel = new JPanel();
		resultTitlePanel.add(new JLabel("����������"));

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
	 * �ڲ��������࣬���ڼ��������á���ť
	 * 
	 * @author WangChao
	 */
	class ResetButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			int response = JOptionPane.showConfirmDialog(frame,
					"�ÿս����֮ǰ������������ݣ�", "��ʾ", JOptionPane.YES_NO_OPTION);

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
	 * �ڲ��������࣬���ڼ��������㡱��ť
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
				JOptionPane.showMessageDialog(frame, "δ�����κ����ݣ�", "������ʾ",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			//��������һ���߳����������û����ӣ���������GUI���߳�
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
	 * �ڲ��������࣬���ڼ������˳�����ť
	 * 
	 * @author WangChao
	 * 
	 */
	class ExitButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			int response = JOptionPane.showConfirmDialog(frame, "ȷ���˳���", "��ʾ",
					JOptionPane.YES_NO_OPTION);

			if (response == 0)
				System.exit(0);
		}
	}
}
