package put.example.employeeStore;

class Employee {
    private long id;
    private String name;
    private String surname;
    private String email;

    public Employee() {
    }

    public Employee(long id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("Employee {id=%d, name='%s', surname='%s', email='%s'}", id, name, surname, email);
    }
}
