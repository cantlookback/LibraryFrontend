package com.libraryfront.rcp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.libraryfront.rcp.entity.Book;
import com.libraryfront.rcp.entity.BookBorrow;
import com.libraryfront.rcp.entity.Person;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

public class JsonParser {
	  private static final Gson gson = new GsonBuilder()
	            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
	            .create();

	    public static List<BookBorrow> parseBookBorrows(String json) {
	        Type listType = new TypeToken<List<BookBorrow>>() {}.getType();
	        return gson.fromJson(json, listType);
	    }
	    
	    public static List<Book> parseBooks(String json) {
	        Type listType = new TypeToken<List<Book>>() {}.getType();
	        return gson.fromJson(json, listType);
	    }
	    
	    public static List<Person> parsePersons(String json) {
	        Type listType = new TypeToken<List<Person>>() {}.getType();
	        return gson.fromJson(json, listType);
	    }
	    
	    public static String toJson(Book book) {
	    	return gson.toJson(book);
	    }
	    
	    public static String toJson(Person person) {
	    	return gson.toJson(person);
	    }
	    
	    public static String toJson(BookBorrow book_borrow) {
	    	return gson.toJson(book_borrow);
	    }
}
