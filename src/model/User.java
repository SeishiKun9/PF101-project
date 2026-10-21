package model;

public abstract class User {
    private String fullName;
    private String password;
    private String role;

    public User(String fullName, String password, String role) {
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    public String getFullName() {
         return fullName; 
        }

    public void setFullName(String fullName) {
         this.fullName = fullName; 
        }

    public String getPassword() {
         return password; 
        }

    public void setPassword(String password) {
         this.password = password; 
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