import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Stack;

public class ClientApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Calculadora Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JPanel panel = new JPanel();
        frame.add(panel);

        // Pantalla de la calculadora
        JTextField display = new JTextField();
        display.setFont(new Font("Arial", Font.PLAIN, 20));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false); // La pantalla es solo para mostrar resultados
        panel.add(display);

        // Botones de la calculadora
        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "^", "+",
                "(", ")", "C", "="
        };

        panel.setLayout(new GridLayout(5, 4, 10, 10)); // Organiza los botones en una cuadrícula de 5x4

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            panel.add(button);

            // Agregar un ActionListener a cada botón
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String buttonText = button.getText();
                    String currentText = display.getText();

                    if (buttonText.equals("=")) {
                        // Cuando se presiona "=", convertir a notación postfija y enviar al servidor
                        String infixExpression = currentText;
                        String postfixExpression = InfixToPostfixConverter.convertToPostfix(infixExpression);
                        sendDataToServer(postfixExpression);
                    } else if (buttonText.equals("C")) {
                        // Limpiar la pantalla
                        display.setText("");
                    } else {
                        // Agregar el texto del botón a la pantalla
                        display.setText(currentText + buttonText);
                    }
                }
            });
        }

        frame.setVisible(true);
    }

    public static void sendDataToServer(String data) {
        try {
            Socket socket = new Socket("localhost", 12345); // Conexión local al servidor en el puerto 12345
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(data);
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class InfixToPostfixConverter {
        public static String convertToPostfix(String infixExpression) {
            StringBuilder output = new StringBuilder();
            Stack<Character> operatorStack = new Stack<>();
            String token = "";

            for (char c : infixExpression.toCharArray()) {
                if (Character.isDigit(c)) {
                    token += c;
                } else {
                    if (!token.isEmpty()) {
                        output.append(token).append(" ");
                        token = "";
                    }
                    if (c == '(') {
                        operatorStack.push(c);
                    } else if (c == ')') {
                        while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                            output.append(operatorStack.pop()).append(" ");
                        }
                        if (!operatorStack.isEmpty() && operatorStack.peek() == '(') {
                            operatorStack.pop(); // Desapila el '('
                        } else {
                            // Maneja el caso de paréntesis desequilibrados si es necesario
                            throw new IllegalArgumentException("Paréntesis desequilibrados");
                        }
                    } else {
                        // Procesa el operador actual
                        while (!operatorStack.isEmpty() && precedence(c) <= precedence(operatorStack.peek())) {
                            output.append(operatorStack.pop()).append(" ");
                        }
                        operatorStack.push(c);
                    }
                }
            }

            if (!token.isEmpty()) {
                output.append(token).append(" ");
            }

            while (!operatorStack.isEmpty()) {
                output.append(operatorStack.pop()).append(" ");
            }

            return output.toString().trim();
        }

        private static int precedence(char operator) {
            if (operator == '+' || operator == '-') {
                return 1;
            } else if (operator == '*' || operator == '/') {
                return 2;
            }
            return 0; // Operadores no reconocidos
        }
    }
}