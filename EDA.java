// Stack, ArrayList, Arrays, Scanner
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;
import org.ejml.simple.SimpleMatrix;

public class EDA {
  public SimpleMatrix C; // C is the adjacency matrix of the circuit
  public SimpleMatrix A;
  public SimpleMatrix B; // B=[bx,by]
  public SimpleMatrix G; // output, G=inv(A)*B
  public int M;
  public int N;
  public int num;

  private int addRow(SimpleMatrix matrix, int row) {
    int sum = 0;
    for (int i = 0; i < matrix.getNumCols(); i++) sum += matrix.get(row, i);
    return sum;
  }

  private void calA() {
    for (int i = 0; i < A.getNumRows(); i++)
      for (int j = 0; j < A.getNumCols(); j++)
        if (i == j) A.set(i, j, addRow(C, i));
        else A.set(i, j, -C.get(i, j));

    for (int i = 0; i < A.getNumRows(); i++)
      if (B.get(i, 0) != 0 || B.get(i, 1) != 0) {
        for (int j = 0; j < A.getNumCols(); j++) A.set(i, j, 0);
        A.set(i, i, 1);
      }
  }

  public void calGates() {
    SimpleMatrix AInv = A.invert();
    G = AInv.mult(B);
  }

  public void writeResult(String filename) {
    try {
      File file = new File(filename);
      java.io.PrintWriter output = new java.io.PrintWriter(file);
      for (int i = 0; i < G.getNumRows(); i++)
        output.println("gate " + (i + 1) + " position: " + G.get(i, 0) + " " + G.get(i, 1));
      output.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public EDA(String filename) {
    try {
      File file = new File(filename);
      Scanner scanner = new Scanner(file);
      M = scanner.nextInt();
      N = scanner.nextInt();
      Stack<Integer> stack = new Stack<>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.equals("-1")) break;
        if (line.equals("")) continue;
        Scanner lineScanner = new Scanner(line);
        lineScanner.next(); // skip blk
        int a = lineScanner.nextInt();
        num = Math.max(num, a);
        while (lineScanner.hasNextInt()) {
          int b = lineScanner.nextInt();
          if (b == -1) {
            break;
          } else {
            stack.push(a);
            stack.push(b);
            num = Math.max(num, b);
          }
        }
      }
      C = new SimpleMatrix(num, num);
      A = new SimpleMatrix(num, num);
      while (!stack.isEmpty()) {
        int b = stack.pop();
        int a = stack.pop();
        C.set(a - 1, b - 1, 1);
        C.set(b - 1, a - 1, 1);
      }
      B = new SimpleMatrix(num, 2);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.equals("-1")) break;
        if (line.equals("")) continue;
        Scanner lineScanner = new Scanner(line);
        lineScanner.next(); // skip blk
        int i = lineScanner.nextInt();
        int x = lineScanner.nextInt();
        int y = lineScanner.nextInt();
        B.set(i - 1, 0, x);
        B.set(i - 1, 1, y);
      }
      calA();
      scanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
