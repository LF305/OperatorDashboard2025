package frc.robot;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ReefTracker extends JFrame {
  public static void main(String[] args) {
    new ReefTracker();
  }

  private JButton[] coralButtons = new JButton[36];
  private JButton[] algaeButtons = new JButton[6];
  private boolean[] coralStates = new boolean[36];
  private boolean[] algaeStates = new boolean[6];

  private JTextArea l4TextCounter;
  private JTextArea l3TextCounter;
  private JTextArea l2TextCounter;
  private JTextArea treyTextCounter;

  private JPanel l4CounterPanel;
  private JPanel l3CounterPanel;
  private JPanel l2CounterPanel;
  private JPanel treyCounterPanel;

  private int L1CoralCount = 0;
  private int L2CoralCount = 0;
  private int L3CoralCount = 0;
  private int L4CoralCount = 0;

  private int levelPriority;

  // private int treyTarget;
  // private int L2Target;
  // private int L3Target;
  // private int L4Target;

  private NetworkTableInstance inst;
  private NetworkTable table;

  DoublePublisher L1Pub;
  DoublePublisher L2Pub;
  DoublePublisher L3Pub;
  DoublePublisher L4Pub;

  DoubleSubscriber L1Sub;
  DoubleSubscriber L2Sub;
  DoubleSubscriber L3Sub;
  DoubleSubscriber L4Sub;

  public ReefTracker() {

    //Setup networktables
    inst = NetworkTableInstance.getDefault();

    inst.setServerTeam(4946); // Preferred way for FRC
    inst.startDSClient();

    table = inst.getTable("ReefTracker");

    L1Pub = table.getDoubleTopic("L1").publish();
    L2Pub = table.getDoubleTopic("L2").publish();
    L3Pub = table.getDoubleTopic("L3").publish();
    L4Pub = table.getDoubleTopic("L4").publish();

    L1Sub = table.getDoubleTopic("L1").subscribe(0.0);
    L2Sub = table.getDoubleTopic("L2").subscribe(0.0);
    L3Sub = table.getDoubleTopic("L3").subscribe(0.0);
    L4Sub =table.getDoubleTopic("L4").subscribe(0.0);

    new Thread(() -> {
      while (true) {
          updateValues(L1Sub, L2Sub, L3Sub, L4Sub);
          try {
              Thread.sleep(100); // Prevents excessive CPU usage
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }
  }).start();


  l4CounterPanel = new JPanel();
  l4CounterPanel.setBackground(new Color(30, 30, 30));
  l4CounterPanel.setBounds(700, 0, 300, 125);
  l4CounterPanel.setLayout(new BorderLayout(1900, 1900));

  l4TextCounter = new JTextArea("L4: " + getL4CoralCount());
  l4TextCounter.setFont(new Font("Ariel", Font.BOLD, 80));
  l4TextCounter.setForeground(new Color(255, 255, 255));
  l4TextCounter.setBackground(new Color(30, 30, 30));
  l4TextCounter.setEditable(false);
  l4TextCounter.setBorder(null);

  l3CounterPanel = new JPanel();
  l3CounterPanel.setBackground(new Color(30, 30, 30));
  l3CounterPanel.setBounds(700, 125, 300, 125);
  l3CounterPanel.setLayout(new BorderLayout(1900, 1900));

  l3TextCounter = new JTextArea("L3: " + getL3CoralCount());
  l3TextCounter.setFont(new Font("Ariel", Font.BOLD, 80));
  l3TextCounter.setForeground(new Color(255, 255, 255));
  l3TextCounter.setBackground(new Color(30, 30, 30));
  l3TextCounter.setEditable(false);
  l3TextCounter.setBorder(null);

  l2CounterPanel = new JPanel();
  l2CounterPanel.setBackground(new Color(30, 30, 30));
  l2CounterPanel.setBounds(700, 250, 300, 125);
  l2CounterPanel.setLayout(new BorderLayout(1900, 1900));

  l2TextCounter = new JTextArea("L2: " + getL4CoralCount());
  l2TextCounter.setFont(new Font("Ariel", Font.BOLD, 80));
  l2TextCounter.setForeground(new Color(255, 255, 255));
  l2TextCounter.setBackground(new Color(30, 30, 30));
  l2TextCounter.setEditable(false);
  l2TextCounter.setBorder(null);

  treyCounterPanel = new JPanel();
  treyCounterPanel.setBackground(new Color(30, 30, 30));
  treyCounterPanel.setBounds(700, 375, 300, 125);
  treyCounterPanel.setLayout(new BorderLayout(1900, 1900));

  treyTextCounter = new JTextArea("L1: " + getL1CoralCount());
  treyTextCounter.setFont(new Font("Ariel", Font.BOLD, 80));
  treyTextCounter.setForeground(new Color(255, 255, 255));
  treyTextCounter.setBackground(new Color(30, 30, 30));
  treyTextCounter.setEditable(false);
  treyTextCounter.setBorder(null);

  l4CounterPanel.add(l4TextCounter, BorderLayout.CENTER);
  l3CounterPanel.add(l3TextCounter, BorderLayout.CENTER);
  l2CounterPanel.add(l2TextCounter, BorderLayout.CENTER);
  treyCounterPanel.add(treyTextCounter, BorderLayout.CENTER);

  add(l4CounterPanel);
  add(l3CounterPanel);
  add(l2CounterPanel);
  add(treyCounterPanel);

  Arrays.fill(coralStates, false);
  Arrays.fill(algaeStates, true);

  setTitle("Operator Dashboard");
  setSize(1600, 1000);
  setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  setLayout(null);
  getContentPane().setBackground(new Color(30, 30, 30));

  int centerX = 320;
  int centerY = 305;
  int hexagonRadius = 110;
  int buttonDiameter = 50;

  int algaeIndex = 0;
  for (int i = 0; i < 6; i++) {

    double angle = 60 * i;

    int initialAlgaeX = centerX;
    int initialAlgaeLowY = centerY - (hexagonRadius + buttonDiameter + ((hexagonRadius - buttonDiameter) / 2));
    int initialAlgaeHighY = centerY - (hexagonRadius + buttonDiameter + (((hexagonRadius - buttonDiameter) / 2) * 3));

    int algaeX;
    int algaeY;

    if (i == 0 || i == 2 || i == 4) {
      algaeX = rotatePoint(initialAlgaeX, initialAlgaeLowY, centerX, centerY, angle)[0];
      algaeY = rotatePoint(initialAlgaeX, initialAlgaeLowY, centerX, centerY, angle)[1];
    } else {
      algaeX = rotatePoint(initialAlgaeX, initialAlgaeHighY, centerX, centerY, angle)[0];
      algaeY = rotatePoint(initialAlgaeX, initialAlgaeHighY, centerX, centerY, angle)[1];
    }

    algaeButtons[algaeIndex] = makeButtonGoCircle(String.valueOf(algaeIndex + 1), false);
    algaeButtons[algaeIndex].setBounds(algaeX, algaeY, buttonDiameter, buttonDiameter);
    final int num1 = algaeIndex;
    algaeButtons[algaeIndex].addActionListener(
        e -> {
          toggleAlgaeState(num1);
        });
    add(algaeButtons[algaeIndex]);
    algaeIndex++;
  }

  int coralIndex = 0;
  for (int layer = 0; layer < 3; layer++) {

    int initialCoralX1 = centerX - (buttonDiameter / 2 + 30);
    int initialCoralY1 = centerY - hexagonRadius * (layer + 1);

    int initialCoralX2 = centerX + (buttonDiameter / 2 + 30);
    int initialCoralY2 = centerY - hexagonRadius * (layer + 1);

    if (layer == 0) {
      initialCoralY1 -= buttonDiameter;
      initialCoralY2 -= buttonDiameter;
    } else if (layer == 2) {
      initialCoralY1 += buttonDiameter;
      initialCoralY2 += buttonDiameter;
    }

    for (int side = 0; side < 6; side++) {

      double angle = 60 * side;

      int coralX1 = rotatePoint(initialCoralX1, initialCoralY1, centerX, centerY, angle)[0];
      int coralY1 = rotatePoint(initialCoralX1, initialCoralY1, centerX, centerY, angle)[1];
      int coralX2 = rotatePoint(initialCoralX2, initialCoralY2, centerX, centerY, angle)[0];
      int coralY2 = rotatePoint(initialCoralX2, initialCoralY2, centerX, centerY, angle)[1];

      coralButtons[coralIndex] = makeButtonGoCircle(String.valueOf(coralIndex + 1), true);
      coralButtons[coralIndex].setBounds(coralX1, coralY1, buttonDiameter, buttonDiameter);
      final int num1 = coralIndex;
      coralButtons[coralIndex].addActionListener(
          e -> {
            toggleCoralState(num1);
          });

      add(coralButtons[coralIndex]);
      coralIndex++;

      coralButtons[coralIndex] = makeButtonGoCircle(String.valueOf(coralIndex + 1), true);
      coralButtons[coralIndex].setBounds(coralX2, coralY2, buttonDiameter, buttonDiameter);
      final int num2 = coralIndex;
      coralButtons[coralIndex].addActionListener(
          e -> {
            toggleCoralState(num2);
          });

      add(coralButtons[coralIndex]);
      coralIndex++;
    }
  }

  JButton treyButton = new JButton("+") {
    @Override
    protected void paintComponent(Graphics g) {
      if (getModel().isPressed()) {
        g.setColor(new Color(0, 255, 0));
      } else {
        g.setColor(new Color(120, 120, 120));
      }
      g.fillRect(0, 0, getWidth(), getHeight());
      super.paintComponent(g);
    }
  };
  treyButton.setFont(new Font("Arial", Font.BOLD, 20));
  treyButton.setContentAreaFilled(false);
  treyButton.setFocusPainted(false);
  treyButton.setBorderPainted(false);
  treyButton.setBounds(centerX + 55, centerY, buttonDiameter, buttonDiameter);
  treyButton.addActionListener(
      e -> {
        L1CoralCount++;
        treyTextCounter.setText("L1: " + getL1CoralCount());
        L1Pub.set(getL1CoralCount());
      });
  JButton subtractButton = new JButton("-") {
    @Override
    protected void paintComponent(Graphics g) {
      if (getModel().isPressed()) {
        g.setColor(new Color(255, 0, 0));
      } else {
        g.setColor(new Color(120, 120, 120));
      }
      g.fillRect(0, 0, getWidth(), getHeight());
      super.paintComponent(g);
    }
  };
  subtractButton.setFont(new Font("Arial", Font.BOLD, 20));
  subtractButton.setContentAreaFilled(false);
  subtractButton.setFocusPainted(false);
  subtractButton.setBorderPainted(false);
  subtractButton.setBounds(centerX - 55, centerY, buttonDiameter, buttonDiameter);
  subtractButton.addActionListener(
      e -> {
        if (L1CoralCount > 0) {
          L1CoralCount--;
        }
        treyTextCounter.setText("L1: " + getL1CoralCount());
        L1Pub.set(getL1CoralCount());
      });
  add(treyButton);
  add(subtractButton);

  setVisible(true);
  }

  private void updateValues(DoubleSubscriber L1Sub, DoubleSubscriber L2Sub, DoubleSubscriber L3Sub, DoubleSubscriber L4Sub){
    double newL1 = L1Sub.get();
    double newL2 = L2Sub.get();
    double newL3 = L3Sub.get();
    double newL4 = L4Sub.get();
  }


  private JButton makeButtonGoCircle(String text, boolean isCoral) {
    JButton button;
    if (isCoral) {
      button = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {

          if (coralStates[Integer.parseInt(getText()) - 1]) {
            g.setColor(new Color(255, 255, 255));
            setForeground(new Color(255, 255, 255));
          } else {
            g.setColor(new Color(50, 50, 50));
            setForeground(new Color(50, 50, 50));
          }

          g.fillOval(0, 0, getWidth(), getHeight());
          super.paintComponent(g);
        }
      };
      button.setContentAreaFilled(false);
      button.setFocusPainted(false);
      button.setBorderPainted(false);
    } else {
      button = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {

          if (algaeStates[Integer.parseInt(getText()) - 1]) {
            g.setColor(new Color(0, 255, 170));
            setForeground(new Color(0, 255, 170));
          } else {
            g.setColor(new Color(45, 45, 45));
            setForeground(new Color(45, 45, 45));
          }

          g.fillOval(0, 0, getWidth(), getHeight());
          super.paintComponent(g);
        }
      };
      button.setContentAreaFilled(false);
      button.setFocusPainted(false);
      button.setBorderPainted(false);
    }
    return button;
  }

  public void toggleCoralState(int coralIndex) {

    if (coralIndex + 1 <= 12) {
      L2CoralCount = coralStates[coralIndex] ? L2CoralCount - 1 : L2CoralCount + 1;
      l2TextCounter.setText("L2: " + getL2CoralCount());
      L2Sub.get(getL2CoralCount());
    } else if (coralIndex + 1 <= 24) {
      L3CoralCount = coralStates[coralIndex] ? L3CoralCount - 1 : L3CoralCount + 1;
      l3TextCounter.setText("L3: " + getL3CoralCount());
      L3Sub.get(getL3CoralCount());
    } else {
      L4CoralCount = coralStates[coralIndex] ? L4CoralCount - 1 : L4CoralCount + 1;
      l4TextCounter.setText("L4: " + getL4CoralCount());
      L3Sub.get(getL3CoralCount());
    }

    coralStates[coralIndex] = !coralStates[coralIndex];
  }

  public void toggleAlgaeState(int algaeIndex) {

    algaeStates[algaeIndex] = !algaeStates[algaeIndex];
  }

  public int getL1CoralCount() {
    return L1CoralCount;
  }

  public int getL2CoralCount() {
    return L2CoralCount;
  }

  public int getL3CoralCount() {
    return L3CoralCount;
  }

  public int getL4CoralCount() {
    return L4CoralCount;
  }

  public void setLevelPriority(int priority) {
    levelPriority = priority;
  }

  public static int[] rotatePoint(
      double x, double y, double centerX, double centerY, double degrees) {

    double xPrime = x - centerX;
    double yPrime = y - centerY;

    double xNew = xPrime * Math.cos(Math.toRadians(degrees)) - yPrime * Math.sin(Math.toRadians(degrees));
    double yNew = xPrime * Math.sin(Math.toRadians(degrees)) + yPrime * Math.cos(Math.toRadians(degrees));

    xNew += centerX;
    yNew += centerY;

    return new int[] { (int) xNew, (int) yNew };
  }
  
}
