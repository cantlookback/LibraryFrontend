package com.libraryfront.rcp.entity;

import java.time.LocalDateTime;

public class BookBorrow {
	private Long id;
    private Person person;
    private Book book;
    private boolean is_borrowed;
    private LocalDateTime date;
    
    public BookBorrow(Long id, Person person, Book book, boolean is_borrowed, LocalDateTime date) {
    	this.id = id;
    	this.person = person;
    	this.book = book;
    	this.is_borrowed = is_borrowed;
    	this.date = date;
    }
    
    public BookBorrow(Person person, Book book, boolean is_borrowed, LocalDateTime date) {
    	this.person = person;
    	this.book = book;
    	this.is_borrowed = is_borrowed;
    	this.date = date;
    }

    public Long getId() {
    	return this.id;
    }
    
    public Person getPerson() {
    	return this.person;
    }
    
    public Book getBook() {
    	return this.book;
    }
    
    public LocalDateTime getDate() {
    	return this.date;
    }
    
    public boolean isBorrowed() {
    	return this.is_borrowed;
    }

    // Конструктор, геттеры и сеттеры
}