import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        System.out.println("\n\nTest Part2(Question2):");
        String path1 = "test_file_part2_1.txt";

        EvaluationInfixExpression test_q2 = new EvaluationInfixExpression(path1);
        try {
            test_q2.doOperation();
        } catch (IOException e) {
            e.printStackTrace();
        }

        path1 = "test_file_part2_2.txt";

        test_q2 = new EvaluationInfixExpression(path1);
        try {
            test_q2.doOperation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
