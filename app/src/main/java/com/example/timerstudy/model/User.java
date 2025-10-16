package com.example.timerstudy.model;

/**
 * Model class đại diện cho thông tin người dùng
 * Sử dụng trong MVVM pattern để truyền dữ liệu giữa các layer
 */
public class User {
    private String name;
    private int age;
    private String email;
    private String bio;

    // Constructor
    public User(String name, int age, String email, String bio) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.bio = bio;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
