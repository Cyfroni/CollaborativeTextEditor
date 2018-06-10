////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//package learn;
//
//import java.awt.Component;
//import java.awt.EventQueue;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import javax.swing.GroupLayout;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JTextField;
//import javax.swing.GroupLayout.Alignment;
//import javax.swing.LayoutStyle.ComponentPlacement;
//
//public class CelsiusConverterGUI extends JFrame {
//    private JLabel celsiusLabel;
//    private JButton convertButton;
//    private JLabel fahrenheitLabel;
//    private JTextField tempTextField;
//
//    public CelsiusConverterGUI() {
//        this.initComponents();
//    }
//
//    private void initComponents() {
//        this.tempTextField = new JTextField();
//        this.celsiusLabel = new JLabel();
//        this.convertButton = new JButton();
//        this.fahrenheitLabel = new JLabel();
//        this.setDefaultCloseOperation(3);
//        this.setTitle("Celsius Converter");
//        this.celsiusLabel.setText("Celsius");
//        this.convertButton.setText("Convert");
//        this.convertButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                CelsiusConverterGUI.this.convertButtonActionPerformed(evt);
//            }
//        });
//        this.fahrenheitLabel.setText("Fahrenheit");
//        GroupLayout layout = new GroupLayout(this.getContentPane());
//        this.getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.tempTextField, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.celsiusLabel)).addGroup(layout.createSequentialGroup().addComponent(this.convertButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.fahrenheitLabel))).addContainerGap(27, 32767)));
//        layout.linkSize(0, new Component[]{this.convertButton, this.tempTextField});
//        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.tempTextField, -2, -1, -2).addComponent(this.celsiusLabel)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.convertButton).addComponent(this.fahrenheitLabel)).addContainerGap(21, 32767)));
//        this.pack();
//    }
//
//    private void convertButtonActionPerformed(ActionEvent evt) {
//        int tempFahr = (int)(Double.parseDouble(this.tempTextField.getText()) * 1.8D + 32.0D);
//        this.fahrenheitLabel.setText(tempFahr + " Fahrenheit");
//    }
//
//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                (new CelsiusConverterGUI()).setVisible(true);
//            }
//        });
//    }
//}
//
