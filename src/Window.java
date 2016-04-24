/* Name: Youssef Al Hindi
 Course: CNT 4714 – Spring 2016
 Assignment title: Program 1 – Event-driven Programming
 Date: Sunday January 24, 2016
*/

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class Window extends JFrame {
    public String[][] inventory = new String[10000][3];
    public int NumOfItems = 0;
    public float taxRate = 6;
    private JTextField NumOfItemsField;
    private JTextField BookIDField;
    private JTextField QuantityField;
    private JTextField BookInfoField;
    private JTextField SubtotalField;
    private JButton ProcessItemButton;
    private JButton ConfirmItemButton;
    private JButton ViewOrderButton;
    private JButton FinishOrderButton;
    private JButton NewOrderButton;
    private JButton ExitButton;
    private JLabel BookIDLabel;
    private JLabel BookQuantityLabel;
    private JLabel InfoLabel;
    private JLabel SubtotalLabel;
    private JPanel rootPanel;
    private int currentStep = 1;
    private int BookID = 0;
    private int Quantity = 0;
    private int index = -1;
    private float Discount = 0;
    private float subtotal = 0;
    private String info = "";
    private ArrayList order = new ArrayList();

    public Window() {
        super("E-Store");
        setContentPane(rootPanel);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initArray(inventory);

        String inventoryFile = "inventory.txt";
        try {
            readInventory(inventoryFile, inventory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        setVisible(true);
        ProcessItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NumOfItems = Integer.parseInt(NumOfItemsField.getText());
                BookID = Integer.parseInt(BookIDField.getText());
                Quantity = Integer.parseInt(QuantityField.getText());
                index = lookup(BookIDField.getText(), inventory);
                Discount = getDiscount(Quantity);
                float itemTotal = 0;
                if (index != -1) {
                    itemTotal = applyDiscount(Float.parseFloat(inventory[index][2]), Discount);
                    info = inventory[index][0] + ", " + inventory[index][1] + ", " + inventory[index][2] + ", " + Quantity + ", " + Discount + ", " + itemTotal;
                    subtotal = subtotal + (itemTotal * Quantity);
                    SubtotalField.setText(Float.toString(subtotal));
                }

                BookInfoField.setText(info);
                ProcessItemButton.setEnabled(false);
                ConfirmItemButton.setEnabled(true);
                NumOfItemsField.setEnabled(false);
                InfoLabel.setText("Item #" + currentStep + " info:");
                SubtotalLabel.setText("Order subtotal for " + currentStep + " item(s):");

            }
        });

        ExitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        ConfirmItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index == -1) {
                    JOptionPane.showMessageDialog(null, "Book ID " + BookID + " not in file");
                } else {
                    addToOrder(info, order);
                    JOptionPane.showMessageDialog(null, "Item #" + currentStep + " accepted");
                }
                if (currentStep < NumOfItems) {
                    currentStep++;
                    ProcessItemButton.setEnabled(true);
                    ConfirmItemButton.setEnabled(false);

                    BookIDLabel.setText("Enter Book ID for Item #" + currentStep + ":");
                    BookQuantityLabel.setText("Enter quantity for Item #" + currentStep + ":");
                    ProcessItemButton.setText("Process Item #" + currentStep);
                    ConfirmItemButton.setText("Confirm Item #" + currentStep);
                    QuantityField.setText("");
                    BookIDField.setText("");


                } else {
                    BookIDLabel.setText("");
                    BookQuantityLabel.setText("");
                    QuantityField.setText("");
                    BookIDField.setText("");
                    ProcessItemButton.setEnabled(false);
                    ConfirmItemButton.setEnabled(false);
                    ViewOrderButton.setEnabled(true);
                    FinishOrderButton.setEnabled(true);
                    BookIDField.setEnabled(false);
                    QuantityField.setEnabled(false);

                }
            }
        });
        NewOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStep = 1;
                BookID = 0;
                Quantity = 0;
                Discount = 0;
                index = -1;
                subtotal = 0;
                info = "";
                order.clear();
                NumOfItems = 0;

                ProcessItemButton.setEnabled(true);
                ConfirmItemButton.setEnabled(false);
                ViewOrderButton.setEnabled(false);
                FinishOrderButton.setEnabled(false);
                BookIDField.setEnabled(true);
                QuantityField.setEnabled(true);

                BookIDLabel.setText("Enter Book ID for Item #" + currentStep + ":");
                BookQuantityLabel.setText("Enter quantity for Item #" + currentStep + ":");
                InfoLabel.setText("Item #" + currentStep + " info:");
                SubtotalLabel.setText("Order subtotal for 0 item(s):");
                NumOfItemsField.setEnabled(true);
                NumOfItemsField.setText("");
                BookInfoField.setText("");
                SubtotalField.setText("");
                QuantityField.setText("");
                BookIDField.setText("");
                ProcessItemButton.setText("Process Item #" + currentStep);
                ConfirmItemButton.setText("Confirm Item #" + currentStep);
            }
        });
        ViewOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, viewOrder(order));
            }
        });
        FinishOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DateFormat dateFormat = new SimpleDateFormat("DD/MM/YY HH:mm:ss z");
                dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
                Date date = new Date();
                float tax = ((taxRate / 100) * subtotal);
                float total = subtotal + tax;
                String invoice = "Date: " + dateFormat.format(date) + "\n\nNumber of items: " + NumOfItems
                        + "\n\nItem# / ID / Title / Price / Qty / Disc % / Subtotal\n\n" + viewOrder(order)
                        + "\n\nOrder subtotal:  $" + subtotal + "\n\nTax Rate:  " + taxRate + "%\n\nTax amount:  $"
                        + tax + "\n\nOrder Total:  $" + total + "\n\nThanks for shopping at Mark's World of Music!";

                addLog(order);
                JOptionPane.showMessageDialog(null, invoice);
                System.exit(0);
            }
        });
    }

    public static void readInventory(String csvFile, String[][] inventory) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(csvFile));
        scanner.useDelimiter("[\n,]");
        int row = 0, col = 0;
        while (scanner.hasNext()) {

            if (col >= 3) {
                col = 0;
                row++;
            }
            String text = scanner.next();

            inventory[row][col] = text.replaceAll("\\r", "");

            col++;
        }
        scanner.close();
    }

    public static int lookup(String id, String[][] db) {
        for (int i = 0; i < db.length; i++) {
            if (db[i][0].equals(id))
                return i;
        }
        return -1;
    }

    public static String viewOrder(ArrayList list) {
        String out = "";
        for (int i = 0; i < list.size(); i++) {
            out = out + (i + 1) + ". " + list.get(i) + "\n";
        }
        return out;
    }

    public static void addLog(ArrayList list) {
        DateFormat logDateFormat = new SimpleDateFormat("YYMMDDHHmmss");
        logDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        DateFormat dateFormat = new SimpleDateFormat("DD/MM/YY HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        Date date = new Date();

        BufferedWriter bw = null;
        for (int i = 0; i < list.size(); i++) {
            try {
                bw = new BufferedWriter(new FileWriter("transactions.txt", true));
                bw.write(logDateFormat.format(date) + ", " + list.get(i) + ", " + dateFormat.format(date));
                bw.newLine();
                bw.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (bw != null) try {
                    bw.close();
                } catch (IOException ioe2) {
                    // ignore
                }
            }
        }
    }

    public static void addToOrder(String input, ArrayList list) {
        list.add(input);
    }

    public static void initArray(String[][] arry) {
        for (int i = 0; i < 10000; i++) {
            for (int z = 0; z < 3; z++) {
                arry[i][z] = "";
            }
        }
    }

    public static float getDiscount(int num) {
        if (num <= 4) {
            return 0;
        } else if (num <= 9) {
            return 10;
        } else if (num <= 14) {
            return 15;
        } else if (num >= 15) {
            return 20;
        }
        return 0;
    }

    public static float applyDiscount(float cost, float discount) {
        return cost - ((discount / 100) * cost);
    }
}