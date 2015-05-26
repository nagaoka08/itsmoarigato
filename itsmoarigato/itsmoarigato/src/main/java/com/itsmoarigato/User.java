package com.itsmoarigato;

public class User {
	private final String email;
	private final String name;
	public User(String email, String name) {
		super();
		this.email = email;
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public String getName() {
		return name;
	}
}
