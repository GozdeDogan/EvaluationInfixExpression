import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.StringTokenizer;
import java.util.function.DoubleToLongFunction;

public class EvaluationInfixExpression {

    private String path;
    private LinkedStack < Character > operatorStack;
    private LinkedStack < Double > operandStack;
    private static final String OPERATORS = "+-*/";
    private static final int[] PRECEDENCE = {1, 1, 2, 2};
    private StringBuilder postfix;
    private String expression;
    private KWArrayList<Integer> variableValues;
    private KWArrayList<String> variables;


    public EvaluationInfixExpression(String path) {
        this.path = path;
        variableValues = new KWArrayList<Integer>();
        variables = new KWArrayList<String>();
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        if(path != null)
            this.path = path;
    }

    public void readInfixExpreesion() throws IOException {
        String line = null;
        try {
            FileReader fileReader = new FileReader(getPath());
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                if(line != null) {
                    if (line.startsWith("(")) {
                        expression = line;
                    } else {
                        StringTokenizer tok1 = new StringTokenizer(line, "=");
                        while (tok1.hasMoreElements()) {
                            String temp = (String) tok1.nextElement();
                            if(temp != null) {
                                variables.add(temp);
                                variableValues.add(Integer.parseInt((String) tok1.nextElement()));
                            }
                        }
                    }
                }
            }
            bufferedReader.close();

            /*System.out.println("expression: " + expression);
            for (int i = 0; i < variables.size(); i++){
                System.out.println(variables.get(i) + " = " + variableValues.get(i));
            }*/
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + getPath() + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + getPath() + "'");
            // ex.printStackTrace();
        }
    }

    /**
     *
     * @param expression
     * @return
     * @throws Exception
     * @author Koffman & Wolfgang
     */
    public double eval(String expression) throws Exception {
        // Create an empty stack.
        operandStack = new LinkedStack <Double> ();

        // Process each token.
        StringTokenizer tokens = new StringTokenizer(expression);
        try {
            while (tokens.hasMoreTokens()) {
                String nextToken = tokens.nextToken();
                // Does it start with a digit?
                if (Character.isDigit(nextToken.charAt(0))) {
                    // Get the integer value.
                    double value = Double.parseDouble(nextToken);
                    // Push value onto operand stack.
                    operandStack.push(value);
                } // Is it an operator?
                else if (isOperator(nextToken.charAt(0))) {
                    // Evaluate the operator.
                    double result = evalOp(nextToken.charAt(0));
                    // Push result onto the operand stack.
                    operandStack.push(result);
                }
                else {
                    // Invalid character.
                    throw new Exception("Invalid character encountered");
                }
            } // End while.

            // No more tokens - pop result from operand stack.
            double answer = operandStack.pop();
            // Operand stack should be empty.
            if (operandStack.empty()) {
                return answer;
            }
            else {
                // Indicate syntax error.
                throw new Exception("Syntax Error: Stack should be empty");
            }
        }
        catch (EmptyStackException ex) {
            // Pop was attempted on an empty stack.
            throw new Exception("Syntax Error: The stack is empty");
        }
    }

    /**
     *
     * @param infix
     * @return
     * @throws Exception
     * @author Koffman & Wolfgang
     */
    public String convert(String infix) throws Exception {
        operatorStack = new LinkedStack < Character > ();
        postfix = new StringBuilder();
        StringTokenizer infixTokens = new StringTokenizer(infix);
        try {
            while (infixTokens.hasMoreTokens()) {
                String nextToken = infixTokens.nextToken();

                /* fonksiyonalr yerine degerleri postfix'e eklendi*/
                int controlIsFunction = isFunction(nextToken);
                if(controlIsFunction != 0){
                    double value = 0;
                    String arg = infixTokens.nextToken();
                    int index = controlVariables(arg);
                    if(index != -1){
                        value = variableValues.get(index);
                    }
                    else{
                        value = Double.parseDouble(arg);
                    }

                    double result = 0;
                    if(controlIsFunction == 1){
                        result = cosFunction(value);
                    }
                    else if(controlIsFunction == 2){
                        result = sinFunction(value);
                    }
                    else if(controlIsFunction == 3){
                        result = absFunction(value);
                    }

                    postfix.append(result);
                    postfix.append(' ');

                    String temp = infixTokens.nextToken(); /* function right paranthesis */
                }
                else {
                    char firstChar = nextToken.charAt(0);
                    // Is it an operand?
                    if (Character.isJavaIdentifierStart(firstChar) || Character.isDigit(firstChar)) {
                        int index = controlVariables(nextToken);
                        if (index != -1) {
                            postfix.append(variableValues.get(index));
                            postfix.append(' ');
                        } else {
                            postfix.append(nextToken);
                            postfix.append(' ');
                        }
                    } // Is it an operator?
                    else if (isOperator(firstChar)) {
                        processOperator(firstChar);
                    }
                    /*else {
                        throw new Exception("Unexpected Character Encountered: " + firstChar);
                    }*/
                }
            } // End while.

            // Pop any remaining operators and
            // append them to postfix.
            while (!operatorStack.empty()) {
                char op = operatorStack.pop();
                postfix.append(op);
                postfix.append(' ');
            }
            // assert: Stack is empty, return result.
            return postfix.toString();
        }
        catch (EmptyStackException ex) {
            throw new Exception("Syntax Error: The stack is empty");
        }
    }

    /**
     *
     * @param op
     * @author Koffman & Wolfgang
     */
    private void processOperator(char op) {
        if (operatorStack.empty()) {
            operatorStack.push(op);
        }
        else {
            // Peek the operator stack and
            // let topOp be top operator.
            char topOp = operatorStack.peek();
            if (precedence(op) > precedence(topOp)) {
                operatorStack.push(op);
            }
            else {
                // Pop all stacked operators with equal
                // or higher precedence than op.
                while (!operatorStack.empty()
                        && precedence(op) <= precedence(topOp)) {
                    operatorStack.pop();
                    postfix.append(topOp);
                    postfix.append(' ');
                    if (!operatorStack.empty()) {
                        // Reset topOp.
                        topOp = operatorStack.peek();
                    }
                }
                // assert: Operator stack is empty or
                //         current operator precedence >
                //         top of stack operator precedence.
                operatorStack.push(op);
            }
        }
    }

    /**
     *
     * @param ch
     * @return
     * @author Koffman & Wolfgang
     */
    private boolean isOperator(char ch) {
        return OPERATORS.indexOf(ch) != -1;
    }

    /**
     *
     * @param op
     * @return
     * @author Koffman & Wolfgang
     */
    private int precedence(char op) {
        return PRECEDENCE[OPERATORS.indexOf(op)];
    }

    /**
     *
     * @param str
     * @return
     */
    private int isFunction(String str){
        if(str.compareTo("cos(") == 0){
            return 1;
        }
        else if(str.compareTo("sin(") == 0){
            return 2;
        }
        else if(str.compareTo("abs(") == 0){
            return 3;
        }

        return 0;
    }

    /**
     *
     * @param op
     * @return
     * @author Koffman & Wolfgang
     */
    private double evalOp(char op) {
        // Pop the two operands off the stack.
        double rhs = operandStack.pop();
        double lhs = operandStack.pop();
        double result = 0;
        // Evaluate the operator.
        switch (op) {
            case '+':
                result = lhs + rhs;
                break;
            case '-':
                result = lhs - rhs;
                break;
            case '/':
                result = lhs / rhs;
                break;
            case '*':
                result = lhs * rhs;
                break;

        }
        return result;
    }

    /** Compute the sine of an angle in degrees.
     *@param x The angle in degrees
     *@return The sine of x
     *@author Koffman & Wolfgang
     */
    private double sinFunction(double x) {
        if (x < 0) {
            x = -x;
        }
        x = x % 360;
        if (0 <= x && x <= 45) {
            return sin0to45(x);
        }
        else if (45 <= x && x <= 90) {
            return sin45to90(x);
        }
        else if (90 <= x && x <= 180) {
            return sinFunction(180 - x);
        }
        else {
            return -sinFunction(x - 180);
        }
    }

    /**
     * Compute the cosine of an angle in degrees.
     * @param x The angle in degrees
     * @return The cosine of x
     * @author Koffman & Wolfgang
     */
    private double cosFunction(double x){
        return sinFunction(x + 90);
    }

    /**
     *
     * @param x
     * @return
     */
    private double absFunction(double x){
        if(x < 0)
            return x*(-1);
        else
            return x;
    }

    /**
     * Compute the sine of an angle in degrees
     * between 0 and 45.
     * pre: 0 <= x < 45
     * @param x The angle
     * @return The sine of x
     * @author Koffman & Wolfgang
     */
    private static double sin0to45(double x) {
        int[] coef = { -81, -277, 1747900, -1600};
        return polyEval(x, coef) / 10000;
    }

    /**
     * Compute the sine of an angle in degrees
     * between 45 and 90.
     * pre: 45 <= x <= 90
     * @param x The angle
     * @return The sine of x
     * @author Koffman & Wolfgang
     */
    private static double sin45to90(double x) {
        int[] coef = {336, -161420, 75484, 999960000};
        return polyEval(90 - x, coef) / 100000;
    }

    /**
     *
     * @param x
     * @param coef
     * @return
     * @author Koffman & Wolfgang
     */
    private static double polyEval(double x, int[] coef) {
        double result = 0;
        for (int i = 0; i < coef.length; i++) {
            result *= x;
            result += coef[i];
        }
        return result/10000;
    }

    /**
     *
     * @param controlValue
     * @return
     */
    private int controlVariables(String controlValue){
        for (int i = 0; i < variables.size(); i++){
            if(variables.get(i).compareTo(controlValue) == 0)
                return i;
        }
        return -1;
    }

    /**
     *
     * @throws IOException
     */
    public void doOperation() throws IOException {
        readInfixExpreesion();
        try {
            System.out.println("expression: " + expression);
            String result = convert(expression);
            double res = eval(result);
            System.out.println("hesaplama sonucu: " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
