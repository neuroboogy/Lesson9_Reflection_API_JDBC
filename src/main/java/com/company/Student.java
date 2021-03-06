package com.company;

@Table(title = "Students")
public class Student {
    private String name;
    private int age;
    private int score;
    private int height;
    private int weight;

    @Column(title = "Name")
    public String getName() {
        return name;
    }

    @Column(title = "Age")
    public int getAge() {
        return age;
    }

    @Column(title = "Score")
    public int getScore() {
        return score;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public Student(String name, int age, int score, int height, int weight) {
        this.name = name;
        this.age = age;
        this.score = score;
        this.height = height;
        this.weight = weight;
    }
}
