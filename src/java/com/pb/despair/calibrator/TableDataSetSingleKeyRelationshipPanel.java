/*
 * Created on Oct 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.pb.despair.calibrator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import com.hbaspecto.util.TableTransferHandler;


/**
 * @author jabraham
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TableDataSetSingleKeyRelationshipPanel extends TableDataSetMultipleRatioRelationshipPanel implements ActionListener, DocumentListener {

    TableDataSetSingleKeyFieldRelationship myModel;
    SpecificsTable specificsTable;
    JTextField specificElementFieldName;
    JCheckBox intKeyBox;
    JButton addButton;
    JButton deleteButton;
    JTextField nameField;

    /**
     * 
     */
    public TableDataSetSingleKeyRelationshipPanel() {
        super();
        initialize();
    }

    public JTextField getNameField() {
        if (nameField == null) {
            nameField = new JFormattedTextField();
            nameField.setColumns(5);
            nameField.setText(getMyModel().getName());
            nameField.getDocument().addDocumentListener(this);
        }
        return nameField;
    }
    
    public JFormattedTextField getWeightField() {
        if (weight == null) {
            weight = new JFormattedTextField(NumberFormat.getNumberInstance());
            weight.setColumns(10);
            weight.setValue(new Double(getMyModel().getWeight()));
            weight.getDocument().addDocumentListener(this);
        }
        return weight;
    }
    
    public static void main(String[] args) {
        JFrame dialog = new JFrame();
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TableDataSetSingleKeyRelationshipPanel aPanel = new TableDataSetSingleKeyRelationshipPanel();
        dialog.getContentPane().add(aPanel);
        dialog.setSize(aPanel.getPreferredSize());
        dialog.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == getIntKeyBox()) {
            getMyModel2().setIntKey(getIntKeyBox().isSelected());
        }
    }
    boolean initialized;
    void initialize() {
        if (!initialized) {
            initialized = true;
            super.initialize();
            JTable keysTable = new JTable(specificsTable);
            keysTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            keysTable.setDragEnabled(true);
            keysTable.setTransferHandler(new TableTransferHandler());
            specificsPanel = new JPanel();
            specificsPanel.setLayout(new BorderLayout());
            
            JScrollPane keysPane = new JScrollPane(keysTable);
            keysPane.setPreferredSize(new Dimension(200,200));
            keysPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(1),"Key / Target"));
            specificsPanel.add(keysPane, BorderLayout.CENTER);
            getAddButton().addActionListener(specificsTable);
            getDeleteButton().addActionListener(specificsTable);
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(getAddButton());
            buttonPanel.add(getDeleteButton());
            JPanel southPanel = new JPanel();
            southPanel.add(buttonPanel);
            JPanel jp = new JPanel(new GridLayout(0,2));
            jp.add(getSpecificElementFieldName());
            getSpecificElementFieldName().getDocument().addDocumentListener(this);
            jp.add(new JLabel("Key field for multiple values"));
            southPanel.add(jp);
            jp = new JPanel(new GridLayout(0,2));
            jp.add(getWeightField());
            jp.add(new JLabel("Weight"));
            southPanel.add(jp);
            jp = new JPanel(new FlowLayout());
            jp.add(getNameField());
            jp.add(new JLabel("Name of target set"));
            southPanel.add(jp);
            southPanel.add(getIntKeyBox());
            southPanel.add(new JLabel("keyed by int"));
            southPanel.setPreferredSize(new Dimension(500,200));
            specificsPanel.add(southPanel, BorderLayout.SOUTH);
            specificsPanel.setPreferredSize(new Dimension(500,500));
            this.add(specificsPanel);
        }

       }
    
/**
     * 
     */
    class SpecificsTable extends AbstractTableModel implements ActionListener {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return getMyModel2().getTargetValues().length;
        }

        public Object getValueAt(int arg0, int arg1) {
            if (arg1==0) return getMyModel2().getSpecificElementValues()[arg0];
            else return String.valueOf(getMyModel2().getTargetValues()[arg0]); 
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int arg0) {
            if (arg0==0) return "Key Value";
            else return "Target";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int arg0, int arg1) {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object arg0, int arg1, int arg2) {
            if (arg2==0) {
                if (arg0 instanceof String) {
                    getMyModel2().getSpecificElementValues()[arg1]=(String) arg0;
                }
            }
            if (arg2==1) {
                if (arg0 instanceof String) {
                    getMyModel2().getTargetValues()[arg1] = Double.valueOf((String) arg0).doubleValue();
                } 
                if (arg0 instanceof Number) {
                    getMyModel2().getTargetValues()[arg1] = ((Number) arg0).doubleValue();
                }
            }
            fireTableCellUpdated(arg1,arg2);
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand().equals("addRow")) {
                double[] oldTargets = myModel.getTargetValues();
                double[] newTargets = new double[oldTargets.length+1];
                System.arraycopy(oldTargets,0,newTargets,0,oldTargets.length);
                myModel.setTargetValues(newTargets);
                String[] oldKeys = myModel.getSpecificElementValues();
                String[] newKeys = new String[oldKeys.length+1];
                System.arraycopy(oldKeys,0,newKeys,0,oldKeys.length);
                newKeys[oldKeys.length]="";
                myModel.setSpecificElementValues(newKeys);
                getDeleteButton().setEnabled(true);
                fireTableStructureChanged();
            }
            
            if (event.getActionCommand().equals("deleteRow")) {
                double[] oldTargets = myModel.getTargetValues();
                if (oldTargets.length>2) {
                    double[] newTargets = new double[oldTargets.length-1];
                    System.arraycopy(oldTargets,0,newTargets,0,newTargets.length);
                    myModel.setTargetValues(newTargets);
                    String[] oldKeys = myModel.getSpecificElementValues();
                    String[] newKeys = new String[oldKeys.length-1];
                    System.arraycopy(oldKeys,0,newKeys,0,newKeys.length);
                    myModel.setSpecificElementValues(newKeys);
                    if (newTargets.length ==2) getDeleteButton().setEnabled(false);
                    fireTableStructureChanged();
                }
            }
        }

    };
    
    /**
     * @return Returns the myModel.
     */
    public TableDataSetMultipleRatioRelationship getMyModel() {
        if (myModel == null) {
            myModel = new TableDataSetSingleKeyFieldRelationship();
            setMyModel(myModel);
        }
        return myModel;
    }

    /**
     * @return Returns the myModel.
     */
    public TableDataSetSingleKeyFieldRelationship getMyModel2() {
        if (myModel == null) {
            myModel = new TableDataSetSingleKeyFieldRelationship();
            setMyModel(myModel);
        }
        return myModel;
    }
    /**
     * @param myModel The myModel to set.
     */
    public void setMyModel(TableDataSetSingleKeyFieldRelationship myModel) {
        this.myModel = myModel;
        getSpecificsTable().fireTableStructureChanged();
        getWeightField().setValue(new Double(getMyModel().getWeight()));
        getIntKeyBox().setSelected(myModel.isIntKey());
        getSpecificElementFieldName().setText(myModel.getSpecificElementField());
        super.setMyModel(myModel);
    }

    /**
     * @return
     */
    private SpecificsTable getSpecificsTable() {
        if (specificsTable == null) {
            specificsTable = new SpecificsTable();
        }
        return specificsTable;
    }

    /**
     * @return Returns the specificElementFieldName.
     */
    public JTextField getSpecificElementFieldName() {
        if (specificElementFieldName == null) {
            specificElementFieldName = new JTextField();
            specificElementFieldName.setColumns(20);
            specificElementFieldName.getDocument().addDocumentListener(this);
        }
        return specificElementFieldName;
       }

    public JCheckBox getIntKeyBox() {
        if (intKeyBox == null) {
            intKeyBox = new JCheckBox("Keyed by integer field");
            intKeyBox.setToolTipText("Whether the field used to distinguish between elements is an integer field or not (select for int field, deselect for string field)");
            intKeyBox.addActionListener(this);
            intKeyBox.setSelected(getMyModel2().isIntKey());
           }
        return intKeyBox;
       }

    public void changedUpdate(DocumentEvent event) {
        handleDocumentEvent(event);
    }

    public void insertUpdate(DocumentEvent event) {
        handleDocumentEvent(event);
    }

    public void removeUpdate(DocumentEvent event) {
        handleDocumentEvent(event);
    }
    
    private void handleDocumentEvent(DocumentEvent event) {
        if (event.getDocument() == getSpecificElementFieldName().getDocument()) {
            getMyModel2().setSpecificElementField(getSpecificElementFieldName().getText());
        }
        if (event.getDocument() == getWeightField().getDocument()) {
            getMyModel().setWeight(((Number) getWeightField().getValue()).doubleValue());
        }
        if (event.getDocument() == getNameField().getDocument()) {
            getMyModel().setName(getNameField().getText());
        }
    }

    JButton getAddButton() {
        if (addButton == null) {
            addButton = new JButton();
            addButton.setActionCommand("addRow");
            addButton.setText("add row");
           }
        return addButton;
    }

    JButton getDeleteButton() {
        if (deleteButton == null) {
            deleteButton = new JButton();
            deleteButton.setActionCommand("deleteRow");
            deleteButton.setText("delete row");
            if (myModel.getTargetValues().length<2) deleteButton.setEnabled(false);
        }
        return deleteButton;
    }
}