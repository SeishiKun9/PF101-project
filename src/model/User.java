package model;

public abstract class User {
    private String password;
    private String fullName;
    private String role;
    private String address;
    private String contactNo;

    public User(String fullName, String password, String role) {
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? "" : address.trim();
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo == null ? "" : contactNo.trim();
    }


    public String validateContactDetails() {
        if (!"Resident".equals(role)) {
            return null;
        }
        if (address == null || address.trim().isEmpty()) {
            return "Address is required.";
        }
        if (contactNo == null || contactNo.trim().isEmpty()) {
            return "Contact number is required.";
        }
        if (!contactNo.matches("\\+?[0-9]+")) {
            return "Contact number must be numeric.";
        }
        return null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean authenticate(String attemptPassword) {
        return password != null && password.equals(attemptPassword);
    }

    public void logout() {
        System.out.println(fullName + " has logged out.");
    }

    public abstract void showMenu();

    public abstract String getUserId();

    public abstract String toFileLine();
}