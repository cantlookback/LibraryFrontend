package com.libraryfront.rcp.entity;

public class Person {
	private Long id;
    private String name;
    private boolean sex;
    private int age;

    public Person(Long id, String name, boolean sex, int age) {
    	this.id = id;
    	this.name = name;
    	this.sex = sex;
    	this.age = age;
    }
    
    public Person(String name, boolean sex, int age) {
    	this.name = name;
    	this.sex = sex;
    	this.age = age;
    }
    
    public Long getId() {
    	return this.id;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String getSex() {
    	if (this.sex) {
    		return "Male";
    	}
    	return "Female";
    }
    
    public int getAge() {
    	return this.age;
    }
    
    // Конструктор, геттеры и сеттеры
}