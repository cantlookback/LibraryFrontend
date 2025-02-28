package com.libraryfront.rcp.entity;

public class Book {
	private Long id;
    private String name;
    private String author;
    private String publisher;
    private int year;
    private boolean is_available;

    public Book(Long id, String name, String author, String publisher, int year, boolean is_available) {
    	this.id = id;
    	this.name = name;
    	this.author = author;
    	this.publisher = publisher;
    	this.year = year;
    	this.is_available = is_available;
    }
    
    public Book(String name, String author, String publisher, int year, boolean is_available) {
    	this.name = name;
    	this.author = author;
    	this.publisher = publisher;
    	this.year = year;
    	this.is_available = is_available;
    }
    
    public Long getId() {
    	return this.id;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String getAuthor() {
    	return this.author;
    }
    
    public String getPublisher() {
    	return this.publisher;
    }
    
    public int getYear() {
    	return this.year;
    }
    
    public boolean isAvailable() {
    	return this.is_available;
    }
    
    public void setAvailable(boolean available) {
    	this.is_available = available;
    }
    
    // Конструктор, геттеры и сеттеры
}