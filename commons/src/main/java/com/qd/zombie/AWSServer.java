package com.jidian.util;

import java.net.URL;

public class AWSServer {
	private String name;
	private URL path;
	private String type;
	private String elb;

	public AWSServer(String name, URL path, String type, String elb) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.elb = elb;
	}

	public String getName() {
		return name;
	}

	public URL getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public String getElb() {
		return elb;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(URL path) {
		this.path = path;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setElb(String elb) {
		this.elb = elb;
	}
}
