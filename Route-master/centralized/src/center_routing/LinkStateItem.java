package center_routing;

import java.awt.Dimension;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//define the link state item
public class LinkStateItem extends JPanel {
    public JCheckBox checkBox;
    public JLabel label, label2;
    public JTextField field;
    public LinkStateItem() {
        super();
        checkBox = new JCheckBox();
        checkBox.setPreferredSize(new Dimension(20, 20));
        checkBox.setSelected(false);
        label = new JLabel("255.255.255.255");
        label.setPreferredSize(new Dimension(100, 20));
        label2= new JLabel("INF");
        label2.setPreferredSize(new Dimension(30, 20));
        field = new JTextField("INF");
        field.setPreferredSize(new Dimension(30, 20));
        field.setEditable(false);
        field.setVisible(false);
        add(checkBox);
        add(label);
        add(label2);
        add(field);
    }
    public LinkStateItem(boolean ischecked, String ip, String cost) {
        super();
        checkBox = new JCheckBox();
        checkBox.setPreferredSize(new Dimension(20, 20));
        checkBox.setSelected(ischecked);
        label = new JLabel(ip);
        label.setPreferredSize(new Dimension(100, 20));
        label2= new JLabel(cost);
        label2.setPreferredSize(new Dimension(30, 20));
        field = new JTextField(cost);
        field.setPreferredSize(new Dimension(30, 20));
        field.setEditable(ischecked);
        field.setVisible(false);
        add(checkBox);
        add(label);
        add(label2);
        add(field);
    }
}
