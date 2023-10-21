import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;
import java.util.StringTokenizer;

public class ServerApp {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345); // Servidor en el puerto 12345
            System.out.println("Servidor en ejecución. Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                // Manejar la solicitud del cliente en un hilo separado
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String expression = in.readLine();
            System.out.println("Expresión recibida: " + expression);

            try {
                // Aquí, evalúa la expresión matemática utilizando un árbol binario
                double result = evaluateExpression(expression);
                out.println("Resultado: " + result);

                // Agrega esta línea para imprimir el resultado en la consola del servidor
                System.out.println("Resultado: " + result);
            } catch (Exception e) {
                out.println("Error: " + e.getMessage());

                // Agrega esta línea para imprimir el error en la consola del servidor
                System.out.println("Error: " + e.getMessage());
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double evaluateExpression(String expression) {
        ExpressionTree expressionTree = new ExpressionTree(expression);
        return expressionTree.evaluate();
    }
}
class ExpressionTree {
    private Node root;

    private static class Node {
        String value;
        Node left;
        Node right;

        Node(String value) {
            this.value = value;
        }
    }

    public ExpressionTree(String postfixExpression) {
        buildTreeFromPostfix(postfixExpression);
    }

    private void buildTreeFromPostfix(String postfixExpression) {
        Stack<Node> stack = new Stack<>();
        StringTokenizer tokenizer = new StringTokenizer(postfixExpression, " ");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (isOperator(token)) {
                Node right = stack.pop();
                Node left = stack.pop();

                Node operatorNode = new Node(token);
                operatorNode.left = left;
                operatorNode.right = right;

                stack.push(operatorNode);
            } else {
                stack.push(new Node(token));
            }
        }

        root = stack.pop();
    }

    private boolean isOperator(String token) {
        return "+-*/".contains(token);
    }

    public int evaluate() {
        return evaluate(root);
    }

    private int evaluate(Node node) {
        if (node == null)
            return 0;

        if (!isOperator(node.value)) {
            return Integer.parseInt(node.value); // Manejar operandos como números enteros
        }

        int leftValue = evaluate(node.left);
        int rightValue = evaluate(node.right);

        switch (node.value) {
            case "+":
                return leftValue + rightValue;
            case "-":
                return leftValue - rightValue;
            case "*":
                return leftValue * rightValue;
            case "/":
                if (rightValue != 0)
                    return leftValue / rightValue;
                else
                    throw new ArithmeticException("Division by zero");
            default:
                throw new IllegalArgumentException("Invalid operator: " + node.value);
        }
    }
}
