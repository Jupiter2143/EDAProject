import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.ejml.simple.SimpleMatrix;

public class GUI extends JFrame {
  private int padding = 25;
  private int tickSize = 5;
  private int pointSize = 10;
  private int rectW = 600;
  private int rectH = 600;
  private JFrame frame = new JFrame("EDA Project");
  private int width = 650;
  private int height = 650;
  private int M = 50;
  private int N = 50;
  private JMenuBar menuBar = new JMenuBar();
  private MyPanel panel = new MyPanel();
  private JLabel statusBar;
  private EDA eda;

  private class MyPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      drawCoordinateSystem(g2d);
      connectVertices(g2d);
      addVertices(g2d);
    }

    public void addVertices(Graphics2D g) {
      SimpleMatrix G = eda.G;
      for (int i = 0; i < eda.num; i++) {
        g.setColor(Color.RED);
        g.fillOval(
            (int) (G.get(i, 0) * rectW / M + padding - pointSize / 2),
            (int) (height - G.get(i, 1) * rectH / N - padding - pointSize / 2),
            pointSize,
            pointSize);
        g.setColor(Color.decode("#A91D3A"));
        Font font = new Font("Serif", Font.PLAIN, 20);
        Font oldFont = g.getFont();
        g.setFont(font);
        g.drawString(
            String.valueOf(i + 1),
            (int) (G.get(i, 0) * rectW / M + padding + pointSize / 2),
            (int) (height - G.get(i, 1) * rectH / N - padding - pointSize / 2));
        g.setFont(oldFont);
      }
    }

    public void connectVertices(Graphics2D g) {
      g.setColor(Color.BLUE);
      SimpleMatrix G = eda.G;
      for (int i = 0; i < eda.num; i++) {
        for (int j = 0; j < eda.num; j++) {
          if (eda.C.get(i, j) == 1) {
            int x1 = (int) (G.get(i, 0) * rectW / M + padding);
            int y1 = (int) (height - G.get(i, 1) * rectH / N - padding);
            int x2 = (int) (G.get(j, 0) * rectW / M + padding);
            int y2 = (int) (height - G.get(j, 1) * rectH / N - padding);
            g.drawLine(x1, y1, x2, y2);
          }
        }
      }
      g.setColor(Color.BLACK);
    }

    private void drawCoordinateSystem(Graphics2D g) {
      int xTickStep = rectW / M;
      int yTickStep = rectH / N;

      g.drawRect(padding, padding, getWidth() - 2 * padding, getHeight() - 2 * padding);
      for (int i = 0; i <= M; i++) {
        g.drawLine(padding + i * xTickStep, padding, padding + i * xTickStep, padding + tickSize);
        g.drawLine(
            padding + i * xTickStep,
            padding + rectH,
            padding + i * xTickStep,
            padding + rectH - tickSize);
      }
      for (int i = 0; i <= N; i++) {
        g.drawLine(padding, padding + i * yTickStep, padding + tickSize, padding + i * yTickStep);
        g.drawLine(
            padding + rectW,
            padding + i * yTickStep,
            padding + rectW - tickSize,
            padding + i * yTickStep);
      }
      for (int i = 0; i <= M; i += M / 10)
        g.drawString(String.valueOf(i), padding + i * xTickStep, padding + rectH + 15);

      for (int i = 0; i <= N; i += N / 10)
        g.drawString(String.valueOf(i), padding - 15, padding + rectH - i * yTickStep);
    }
  }

  public GUI() {
    if (!calEDA()) System.exit(0);
    initPanel();
    initMenu();
    initStatusBar();
    initMainWindow();
    frame.setVisible(true);
  }

  private boolean calEDA() {
    boolean success = false;
    JFileChooser fileChooser = new JFileChooser(".");
    fileChooser.setDialogTitle("Select input file");
    int returnValue = fileChooser.showOpenDialog(null);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      String filename = fileChooser.getSelectedFile().getAbsolutePath();
      try {
        eda = new EDA(filename);
        try {
          eda.calGates();
          try {
            eda.writeResult("output.txt");
            M = eda.M;
            N = eda.N;
            rectW = width - 2 * padding;
            rectH = height - 2 * padding;
            successOutput();
            success = true;
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame, "Failed to write result", "Error", JOptionPane.ERROR_MESSAGE);
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
              frame, "Failed to calculate gates", "Error", JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            frame, "Invalid input file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    return success;
  }

  private void initMainWindow() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    frame.setLocationRelativeTo(null);
    frame.setJMenuBar(menuBar);
    frame.add(panel, BorderLayout.CENTER);
    frame.add(statusBar, BorderLayout.SOUTH);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    // frame.setBackground(Color.decode("#CAF4FF"));
  }

  private void initPanel() {
    Dimension panelSize = new Dimension(width, height);
    panel.setPreferredSize(panelSize);
    panel.addMouseMotionListener(
        new MouseAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            if (e.getX() < padding
                || e.getX() > width - padding
                || e.getY() < padding
                || e.getY() > height - padding) return;
            int x = (int) ((e.getX() - padding) * M / rectW);
            int y = (int) ((height - e.getY() - padding) * N / rectH);
            statusBar.setText("x: " + x + ", y: " + y);
          }
        });
  }

  private void initMenu() {
    JMenu fileMenu = new JMenu("File");
    JMenuItem openItem = new JMenuItem("Open");
    JMenuItem saveItem = new JMenuItem("Save");
    JMenuItem exitItem = new JMenuItem("Exit");
    fileMenu.add(openItem);
    fileMenu.add(saveItem);
    fileMenu.add(exitItem);
    openItem.addActionListener(
        e -> {
          calEDA();
          panel.repaint();
        });
    saveItem.addActionListener(
        e -> {
          JFileChooser fileChooser = new JFileChooser(".");
          fileChooser.setDialogTitle("Save output file");
          int returnValue = fileChooser.showSaveDialog(null);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            try {
              eda.writeResult(filename);
              successOutput();
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(
                  frame, "Failed to write result", "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    menuBar.add(fileMenu);
    JMenu helpMenu = new JMenu("Help");
    JMenuItem githubItem = new JMenuItem("GitHub");
    helpMenu.add(githubItem);
    githubItem.addActionListener(
        e -> {
          try {
            Desktop.getDesktop()
                .browse(new java.net.URI("https://github.com/Jupiter2143/EDAProject"));
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame, "Failed to open browser", "Error", JOptionPane.ERROR_MESSAGE);
          }
        });
    menuBar.add(helpMenu);

    // menuBar.setBackground(Color.decode("#CAF4FF"));
  }

  private void initStatusBar() {
    statusBar = new JLabel("Status Bar");
    // statusBar.setOpaque(true);
    // statusBar.setBackground(Color.decode("#5AB2FF"));
  }

  private void successOutput() {
    JOptionPane.showMessageDialog(
        frame, "Successfully save the result!", "Success", JOptionPane.INFORMATION_MESSAGE);
  }
}
