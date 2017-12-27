package center_routing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SimpleDialog extends JDialog implements ActionListener {
    JTextField field;
    JList<?> parent;
    JButton setButton;
    JTextField to;
    JLabel jto;
    SimpleDialog(JList<?> parentFrame, JTextField jt, JLabel jLabel) {
        super();
        parent = parentFrame;
        to = jt;
        jto= jLabel;
        JPanel p1 = new JPanel();
        JLabel label = new JLabel("Input Cost:");
        p1.add(label);
        field = new JTextField(30);
        field.setText(jt.getText());
        field.selectAll();
        field.addActionListener(this);
        p1.add(field);
        getContentPane().add("Center", p1);
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("cancel");
        cancelButton.addActionListener(this);
        setButton = new JButton("ok");
        setButton.addActionListener(this);
        p2.add(setButton);
        p2.add(cancelButton);
        getContentPane().add("South", p2);
        pack();
    }
    
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if ((source == setButton)) {
            try {
                String newCost = field.getText();
                if (!"inf".equalsIgnoreCase(newCost)) {
                    int c = Integer.parseInt(newCost);
                    if (c >= 16) {
                        to.setText("INF");
                        jto.setText("INF");
                    } else if (c < 0) {
                        to.setText("0");
                        jto.setText("0");
                    } else {
                        to.setText(newCost);
                        jto.setText(newCost);
                    }
                } else {
                    to.setText(newCost.toUpperCase());
                    jto.setText(newCost.toUpperCase());
                }
                parent.updateUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: Not number " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            }
        }
        setVisible(false);
    }
}