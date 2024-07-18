import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;

class RegistrationForm extends JFrame implements ActionListener {
    private JTextField idField, nameField, contactField, addressField;
    private JRadioButton maleRadioButton, femaleRadioButton;
    private JButton submitButton, resetButton, viewUsersButton;
    private JTextArea displayArea;
    private JComboBox<Integer> yearComboBox, monthComboBox, dayComboBox;

    private Connection connection;

    public RegistrationForm() {
        setTitle("Registration Form");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel displayPanel = new JPanel(new BorderLayout());

        idField = new JTextField();
        nameField = new JTextField();
        contactField = new JTextField();
        addressField = new JTextField();
        maleRadioButton = new JRadioButton("Male");
        femaleRadioButton = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadioButton);
        genderGroup.add(femaleRadioButton);
        submitButton = new JButton("Submit");
        resetButton = new JButton("Reset");
        viewUsersButton = new JButton("View Users");
        displayArea = new JTextArea();
        displayArea.setEditable(false);

        yearComboBox = new JComboBox<>();
        monthComboBox = new JComboBox<>();
        dayComboBox = new JComboBox<>();

        for (int year = 1970; year <= Calendar.getInstance().get(Calendar.YEAR); year++) {
            yearComboBox.addItem(year);
        }
        for (int month = 1; month <= 12; month++) {
            monthComboBox.addItem(month);
        }
        updateDays();

        yearComboBox.addActionListener(e -> updateDays());
        monthComboBox.addActionListener(e -> updateDays());

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Gender:"));
        JPanel genderPanel = new JPanel(new FlowLayout());
        genderPanel.add(maleRadioButton);
        genderPanel.add(femaleRadioButton);
        formPanel.add(genderPanel);
        formPanel.add(new JLabel("Date of Birth:"));
        JPanel dobPanel = new JPanel(new FlowLayout());
        dobPanel.add(yearComboBox);
        dobPanel.add(monthComboBox);
        dobPanel.add(dayComboBox);
        formPanel.add(dobPanel);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

        buttonPanel.add(submitButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(viewUsersButton);

        displayPanel.add(new JLabel("Registered Users:"), BorderLayout.NORTH);
        displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(displayPanel, BorderLayout.EAST);

        submitButton.addActionListener(this);
        resetButton.addActionListener(this);
        viewUsersButton.addActionListener(this);
        initializeDatabaseConnection();
        setVisible(true);
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/registration_db",
                    "root",
                    "mbuva__17"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            registerUser();
        } else if (e.getSource() == resetButton) {
            clearFields();
        } else if (e.getSource() == viewUsersButton) {
            fetchAndDisplayUsers();
        }
    }

    private void registerUser() {
        String id = idField.getText();
        String name = nameField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        String dob = yearComboBox.getSelectedItem() + "-" +
                monthComboBox.getSelectedItem() + "-" +
                dayComboBox.getSelectedItem();
        String contact = contactField.getText();
        String address = addressField.getText();

        showRegistrationDetails(id, name, gender, dob, contact, address);
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
        yearComboBox.setSelectedIndex(0);
        monthComboBox.setSelectedIndex(0);
        dayComboBox.setSelectedIndex(0);
        contactField.setText("");
        addressField.setText("");
    }

    private void updateDays() {
        int year = (int) yearComboBox.getSelectedItem();
        int month = (int) monthComboBox.getSelectedItem();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        dayComboBox.removeAllItems();
        for (int day = 1; day <= daysInMonth; day++) {
            dayComboBox.addItem(day);
        }
    }

    private void showRegistrationDetails(String id, String name, String gender, String dob, String contact, String address) {
        JFrame detailsFrame = new JFrame("Registration Details");
        detailsFrame.setSize(600, 400);
        detailsFrame.setLayout(new BorderLayout());

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.append("ID: " + id + "\n");
        detailsArea.append("Name: " + name + "\n");
        detailsArea.append("Gender: " + gender + "\n");
        detailsArea.append("Date of Birth: " + dob + "\n");
        detailsArea.append("Contact: " + contact + "\n");
        detailsArea.append("Address: " + address + "\n");

        JCheckBox termsCheckBox = new JCheckBox("Accept Terms and Conditions");
        JButton registerButton = new JButton("Register");
        JButton exitButton = new JButton("Exit");

        registerButton.addActionListener(e -> {
            if (termsCheckBox.isSelected()) {
                saveUserToDatabase(id, name, gender, dob, contact, address);
                detailsFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(detailsFrame, "You must accept the terms to register.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> detailsFrame.dispose());

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(termsCheckBox);
        infoPanel.add(registerButton);
        infoPanel.add(exitButton);

        detailsFrame.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        detailsFrame.add(infoPanel, BorderLayout.SOUTH);
        detailsFrame.setVisible(true);
    }

    private void saveUserToDatabase(String id, String name, String gender, String dob, String contact, String address) {
        String sql = "INSERT INTO users (id, name, gender, dob, contact, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, gender);
            preparedStatement.setString(4, dob);
            preparedStatement.setString(5, contact);
            preparedStatement.setString(6, address);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                displayArea.append("User registered: " + name + "\n");
                clearFields();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchAndDisplayUsers() {
        displayArea.setText("");
        String sql = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String gender = resultSet.getString("gender");
                String dob = resultSet.getString("dob");
                String contact = resultSet.getString("contact");
                String address = resultSet.getString("address");
                displayArea.append("ID: " + id + ", Name: " + name + ", Gender: " + gender +
                        ", DOB: " + dob + ", Contact: " + contact + ", Address: " + address + "\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RegistrationForm();
    }
}