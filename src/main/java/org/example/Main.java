package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        try {
            // Путь к SVG-файлу
            File svgFile = new File("src/main/resources/Untitled-1.svg");

            // Создание парсера XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(svgFile);
            doc.getDocumentElement().normalize();

            // Извлечение всех элементов <path>
            NodeList pathList = doc.getElementsByTagName("path");
            int j = 0;
            int count = 0;
            for (int i = 0; i < pathList.getLength(); i++) {
                Node pathNode = pathList.item(i);

                if (pathNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element pathElement = (Element) pathNode;


                    // Получение атрибута "d", содержащего команды кривых
                    String pathData = pathElement.getAttribute("d");
                    if(j != 0){
                        System.out.println("\tfor (int i = 0; i < "+count+"; i++) {\n" +
                                "\t\tpoints[i].x += 300;\n" +
                                "\t\tpoints[i].y -= 10;\n" +
                                "\t}\n" +
                                "\t// Рисование кривой Безье\n" +
                                "\tpDC->PolyBezier(points, "+count+");\n" +
                                "\n" +
                                "}");
                    }
                    j++;

                    count = getCount(pathData);

                    // Разбор команд кривой
                    parsePathData(pathData, count);
                }
            }
            System.out.println("\tfor (int i = 0; i < "+count+"; i++) {\n" +
                    "\t\tpoints[i].x += 300;\n" +
                    "\t\tpoints[i].y -= 10;\n" +
                    "\t}\n" +
                    "\t// Рисование кривой Безье\n" +
                    "\tpDC->PolyBezier(points, "+count+");\n" +
                    "\n" +
                    "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
public static int getCount(String pathData){
    String[] commands = pathData.split("(?=[A-Za-z])"); // Разделение по командам (буквы)
    int count = 1;
    for (int i = 0; i < commands.length-2; i++) {
        count += 3;
    }
    return count;
}
    // Функция для разбора данных кривой
    public static void parsePathData(String pathData, int count) {
        // Пример базовой обработки команды M и c (начальная точка и кривая Безье)
        String[] commands = pathData.split("(?=[A-Za-z])"); // Разделение по командам (буквы)
        int absoluteX = 0;
        int absoluteY = 0;
        int currentX = 0;
        int currentY = 0;
        int j = 0;
        int secondX = 0;
        int secondY = 0;
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];

            if (command.startsWith("M")) {
                // Команда Move to: M x,y
                String[] coords = command.substring(1).trim().split(",");
                if (coords.length == 2) {
                    absoluteX = (int) Math.round(Double.parseDouble(coords[0]));
                    currentX = (int) Math.round(Double.parseDouble(coords[0]));
                    absoluteY = (int) Math.round(Double.parseDouble(coords[1]));
                    currentY = (int) Math.round(Double.parseDouble(coords[1]));
                    System.out.println("\t{\n" +
                            "\t\tCPoint points[" + count+"];\n");
                    System.out.println("points[" + j + "] = { " + currentX + ", " + currentY + " };");
                }
            }     else if (command.startsWith("C")) {
                // Команда Cubic Bézier curve: c x1,y1 x2,y2 x,y (относительные координаты)
                double[] coords = Arrays.stream(command.substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();


                // Проверка на правильное количество координат
                if (coords.length % 3 == 0) {
                    // Вывод управляющих точек и конечной точки
                    int p1x = (int)coords[0];
                    int p1y = (int)coords[1];
                    int p2x = (int)coords[2];
                    secondX = (int)coords[2];
                    int p2y = (int) coords[3];
                    secondY = (int)  coords[3];
                    int p3X = (int) coords[4];
                    int p3y = (int) coords[5];
                    System.out.println(" points[" + ++j + "] = { " + p1x + ", " + p1y + " };");
                    System.out.println(" points[" + ++j + "] = { " + p2x + ", " + p2y + " };");
                    System.out.println(" points[" + ++j + "] = { " + p3X + ", " + p3y + " };");
                    currentX = p3X;
                    currentY = p3y;
                } else {
                    System.out.println("Неверное количество координат для команды c.");
                }
            }
            else if (command.startsWith("c")) {
                // Команда Cubic Bézier curve: c x1,y1 x2,y2 x,y (относительные координаты)
                double[] coords = Arrays.stream(command.substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();


                // Проверка на правильное количество координат
                if (coords.length % 3 == 0) {
                    // Вывод управляющих точек и конечной точки
                    int p1x = (int) Math.round(currentX + coords[0]);
                    int p1y = (int) Math.round(currentY + coords[1]);
                    int p2x = (int) Math.round(currentX + coords[2]);
                    secondX = (int) Math.round(currentX + coords[2]);
                    int p2y = (int) Math.round(currentY + coords[3]);
                    secondY = (int) Math.round(currentY + coords[3]);
                    int p3X = (int) Math.round(currentX + coords[4]);
                    int p3y = (int) Math.round(currentY + coords[5]);
                    System.out.println(" points[" + ++j + "] = { " + p1x + ", " + p1y + " };");
                    System.out.println(" points[" + ++j + "] = { " + p2x + ", " + p2y + " };");
                    System.out.println(" points[" + ++j + "] = { " + p3X + ", " + p3y + " };");
                    currentX = p3X;
                    currentY = p3y;
                } else {
                    System.out.println("Неверное количество координат для команды c.");
                }
            } else if (command.startsWith("s")) {
                double[] coords = Arrays.stream(command.substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();


                // Проверка на правильное количество координат
                // Вывод управляющих точек и конечной точки
                double[] coords2 = Arrays.stream(commands[i - 1].substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();

                int p1x = (int) Math.round(currentX + coords[0]);
                int p1y = (int) Math.round(currentY + coords[1]);
                int p2x = (int) Math.round(currentX + coords[2]);
                int p2y = (int) Math.round(currentY + coords[3]);
                int p3X = 0;
                int p3y = 0;
                if (commands[i - 1].startsWith("c") || commands[i - 1].startsWith("s")) {
                    p3X = (int) 2 * currentX - secondX;
                    p3y = (int) 2 * currentY - secondY;
                } else {
                    p3X = (int) Math.round(currentX + coords2[0]);
                    p3y = (int) Math.round(currentY + coords2[1]);
                }
                secondX = p1x;
                secondY = p1y;
                currentX = p2x;
                currentY = p2y;
                System.out.println(" points[" + ++j + "] = { " + p3X + ", " + p3y + " };");
                System.out.println(" points[" + ++j + "] = { " + p1x + ", " + p1y + " };");
                System.out.println(" points[" + ++j + "] = { " + p2x + ", " + p2y + " };");

            }
            else if (command.startsWith("S")) {
                double[] coords = Arrays.stream(command.substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();


                // Проверка на правильное количество координат
                // Вывод управляющих точек и конечной точки
                double[] coords2 = Arrays.stream(commands[i - 1].substring(1).trim().
                        replace("-", ",-").split(",")).filter(el -> !el.isEmpty()).mapToDouble(el -> Double.parseDouble(el)).toArray();

                int p1x = (int)  coords[0];
                int p1y = (int) coords[1];
                int p2x = (int) coords[2];
                int p2y = (int) coords[3];
                int p3X = 0;
                int p3y = 0;
                if (commands[i - 1].startsWith("c") || commands[i - 1].startsWith("s") ||commands[i - 1].startsWith("C") ) {
                    p3X = (int) 2 * currentX - secondX;
                    p3y = (int) 2 * currentY - secondY;
                } else {
                    p3X = (int) Math.round(currentX + coords2[0]);
                    p3y = (int) Math.round(currentY + coords2[1]);
                }
                secondX = p1x;
                secondY = p1y;
                currentX = p2x;
                currentY = p2y;
                System.out.println(" points[" + ++j + "] = { " + p3X + ", " + p3y + " };");
                System.out.println(" points[" + ++j + "] = { " + p1x + ", " + p1y + " };");
                System.out.println(" points[" + ++j + "] = { " + p2x + ", " + p2y + " };");

            }
        }
    }
}