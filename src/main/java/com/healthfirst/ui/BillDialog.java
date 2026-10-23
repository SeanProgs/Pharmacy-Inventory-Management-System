package com.healthfirst.ui;

import com.healthfirst.model.CartItem;
import com.healthfirst.model.Sale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillDialog extends JDialog {
    private Sale sale;
    private List<CartItem> cartItems;
    private BigDecimal amountPaid;
    private BigDecimal changeDue;
    private JTextPane receiptPane;
    private String rawReceiptText;

    public BillDialog(Frame owner, Sale sale, List<CartItem> cartItems, BigDecimal amountPaid, BigDecimal changeDue) {
        super(owner, "Customer Receipt - Sale #" + sale.getSaleId(), true);
        this.sale = sale;
        this.cartItems = cartItems;
        this.amountPaid = amountPaid;
        this.changeDue = changeDue;
        initUI();
    }

    private void initUI() {
        setSize(520, 680);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Checkout Completed - Tax Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("Invoice #" + sale.getSaleId());
        sub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sub.setForeground(new Color(220, 240, 255));
        headerPanel.add(sub, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Build Receipt Document Content
        receiptPane = new JTextPane();
        receiptPane.setContentType("text/html");
        receiptPane.setEditable(false);
        receiptPane.setBackground(Color.WHITE);

        String htmlContent = generateHtmlReceipt();
        receiptPane.setText(htmlContent);
        receiptPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(receiptPane);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.BG_LIGHT);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton printBtn = UITheme.createStyledButton("🖨 Print Bill", UITheme.PRIMARY, Color.WHITE);
        printBtn.addActionListener(e -> printReceipt());

        JButton saveBtn = UITheme.createStyledButton("💾 Save as Text", UITheme.SUCCESS, Color.WHITE);
        saveBtn.addActionListener(e -> saveReceiptToFile());

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(UITheme.FONT_BOLD);
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(printBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String generateHtmlReceipt() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sale.getSaleDate() != null ? sale.getSaleDate().format(dtf) : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Segoe UI, sans-serif; margin: 15px; color: #222;'>");
        sb.append("<div style='text-align: center; border-bottom: 2px dashed #999; padding-bottom: 10px;'>");
        sb.append("<h2 style='margin: 0; color: #184c78;'>HEALTHFIRST PHARMACY</h2>");
        sb.append("<div style='font-size: 11px; color: #555;'>Healthcare Park, Woodlands Drive, Woodmead</div>");
        sb.append("<div style='font-size: 11px; color: #555;'>Tel: +27 11 555 0100 | Reg: 2026/049182/07</div>");
        sb.append("<div style='font-size: 13px; font-weight: bold; margin-top: 6px;'>TAX INVOICE / CASH SALE</div>");
        sb.append("</div>");

        sb.append("<table style='width: 100%; font-size: 11px; margin: 10px 0;'>");
        sb.append("<tr><td><b>Invoice No:</b> #").append(sale.getSaleId()).append("</td>");
        sb.append("<td style='text-align: right;'><b>Date:</b> ").append(formattedDate).append("</td></tr>");
        sb.append("<tr><td><b>Cashier:</b> ").append(sale.getCashierName() != null ? sale.getCashierName() : "Cashier").append("</td>");
        sb.append("<td style='text-align: right;'><b>Payment:</b> Cash</td></tr>");
        sb.append("</table>");

        sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 11px;'>");
        sb.append("<tr style='background-color: #f0f4f8; border-top: 1px solid #ccc; border-bottom: 1px solid #ccc;'>");
        sb.append("<th style='text-align: left; padding: 6px;'>Item</th>");
        sb.append("<th style='text-align: center; padding: 6px;'>Type</th>");
        sb.append("<th style='text-align: right; padding: 6px;'>Qty</th>");
        sb.append("<th style='text-align: right; padding: 6px;'>Price</th>");
        sb.append("<th style='text-align: right; padding: 6px;'>Total</th>");
        sb.append("</tr>");

        StringBuilder textReceipt = new StringBuilder();
        textReceipt.append("==================================================\n");
        textReceipt.append("             HEALTHFIRST PHARMACY                 \n");
        textReceipt.append("    Healthcare Park, Woodlands Drive, Woodmead    \n");
        textReceipt.append("               Tel: +27 11 555 0100               \n");
        textReceipt.append("            TAX INVOICE / CASH SALE               \n");
        textReceipt.append("==================================================\n");
        textReceipt.append(String.format("Invoice No: #%-15d Date: %s\n", sale.getSaleId(), formattedDate));
        textReceipt.append(String.format("Cashier: %-18s Method: Cash\n", sale.getCashierName() != null ? sale.getCashierName() : "Cashier"));
        textReceipt.append("--------------------------------------------------\n");
        textReceipt.append(String.format("%-22s %4s %9s %9s\n", "Item", "Qty", "Price", "Total"));
        textReceipt.append("--------------------------------------------------\n");

        for (CartItem item : cartItems) {
            sb.append("<tr style='border-bottom: 1px solid #eee;'>");
            sb.append("<td style='padding: 5px;'>").append(item.getMedicine().getName()).append("</td>");
            sb.append("<td style='padding: 5px; text-align: center; color: #666;'>").append(item.getMedicine().getMedicineType()).append("</td>");
            sb.append("<td style='padding: 5px; text-align: right;'>").append(item.getQuantity()).append("</td>");
            sb.append("<td style='padding: 5px; text-align: right;'>R ").append(String.format("%.2f", item.getUnitPrice())).append("</td>");
            sb.append("<td style='padding: 5px; text-align: right; font-weight: bold;'>R ").append(String.format("%.2f", item.getSubtotal())).append("</td>");
            sb.append("</tr>");

            String name = item.getMedicine().getName();
            if (name.length() > 22) name = name.substring(0, 19) + "...";
            textReceipt.append(String.format("%-22s %4d %9.2f %9.2f\n",
                    name, item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
        }

        sb.append("</table>");

        sb.append("<div style='margin-top: 15px; border-top: 2px dashed #999; padding-top: 10px;'>");
        sb.append("<table style='width: 100%; font-size: 12px;'>");
        sb.append("<tr><td style='text-align: right; padding: 3px;'><b>Grand Total:</b></td>");
        sb.append("<td style='text-align: right; font-weight: bold; font-size: 14px; color: #184c78; width: 100px;'>R ").append(String.format("%.2f", sale.getTotalAmount())).append("</td></tr>");

        if (amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("<tr><td style='text-align: right; padding: 3px;'>Amount Paid:</td>");
            sb.append("<td style='text-align: right; width: 100px;'>R ").append(String.format("%.2f", amountPaid)).append("</td></tr>");
            sb.append("<tr><td style='text-align: right; padding: 3px; font-weight: bold;'>Change Due:</td>");
            sb.append("<td style='text-align: right; font-weight: bold; color: #279058; width: 100px;'>R ").append(String.format("%.2f", changeDue != null ? changeDue : BigDecimal.ZERO)).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("</div>");

        sb.append("<div style='text-align: center; margin-top: 25px; border-top: 1px solid #eee; padding-top: 10px; font-size: 10px; color: #777;'>");
        sb.append("Thank you for your patronage! Wishing you speedy recovery.<br>");
        sb.append("<i>Goods once sold cannot be returned without original receipt.</i>");
        sb.append("</div>");

        sb.append("</body></html>");

        textReceipt.append("==================================================\n");
        textReceipt.append(String.format("GRAND TOTAL:                           R %9.2f\n", sale.getTotalAmount()));
        if (amountPaid != null) {
            textReceipt.append(String.format("AMOUNT PAID:                           R %9.2f\n", amountPaid));
            textReceipt.append(String.format("CHANGE DUE:                            R %9.2f\n", changeDue != null ? changeDue : BigDecimal.ZERO));
        }
        textReceipt.append("==================================================\n");
        textReceipt.append("     Thank you for choosing HealthFirst Pharmacy! \n");
        textReceipt.append("==================================================\n");

        this.rawReceiptText = textReceipt.toString();
        return sb.toString();
    }

    private void printReceipt() {
        try {
            boolean complete = receiptPane.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Print job sent successfully.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing error: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceiptToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Receipt_Sale_" + sale.getSaleId() + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(rawReceiptText);
                JOptionPane.showMessageDialog(this, "Receipt saved successfully to:\n" + file.getAbsolutePath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving receipt: " + e.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
