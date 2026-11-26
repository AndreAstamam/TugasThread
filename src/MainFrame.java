import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final StudentDAO dao = new StudentDAO();
    private final JTextField txtNim = new JTextField(10), txtName = new JTextField(15);
    private final JButton btnSave = new JButton("Save"), btnUpdate = new JButton("Update"),
                          btnDelete = new JButton("Delete"), btnRefresh = new JButton("Refresh");
    private final JLabel lblStatus = new JLabel("Ready");
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "NIM", "Name"}, 0);
    private final JTable table = new JTable(model);

    interface DbTask { void run() throws Exception; }

    public MainFrame() {
        super("Student CRUD - Thread & Database");
        JPanel top = new JPanel();
        top.add(new JLabel("NIM:")); top.add(txtNim);
        top.add(new JLabel("Name:")); top.add(txtName);
        top.add(btnSave); top.add(btnUpdate); top.add(btnDelete); top.add(btnRefresh);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                txtNim.setText(model.getValueAt(r, 1).toString());
                txtName.setText(model.getValueAt(r, 2).toString());
            }
        });

        btnSave.addActionListener(e -> runDb("Saving...", () ->
                dao.insert(txtNim.getText(), txtName.getText())));
        btnUpdate.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { lblStatus.setText("Select a row first"); return; }
            int id = (int) model.getValueAt(r, 0);
            runDb("Updating...", () -> dao.update(id, txtNim.getText(), txtName.getText()));
        });
        btnDelete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { lblStatus.setText("Select a row first"); return; }
            int id = (int) model.getValueAt(r, 0);
            runDb("Deleting...", () -> dao.delete(id));
        });
        btnRefresh.addActionListener(e -> loadTable());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        loadTable();
    }

    private void setBusy(boolean busy) {
        btnSave.setEnabled(!busy); btnUpdate.setEnabled(!busy);
        btnDelete.setEnabled(!busy); btnRefresh.setEnabled(!busy);
    }

    // click handler > disable button > show label > DB on a thread > invokeLater > UI
    private void runDb(String message, DbTask task) {
        setBusy(true);
        lblStatus.setText(message);
        new Thread(() -> {
            String result;
            try { task.run(); result = "Done"; }
            catch (Exception ex) { result = "Error: " + ex.getMessage(); }
            String finalResult = result;
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText(finalResult);
                setBusy(false);
                loadTable();
            });
        }).start();
    }

    private void loadTable() {
        new Thread(() -> {
            try {
                List<Object[]> rows = dao.findAll();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    rows.forEach(model::addRow);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblStatus.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}