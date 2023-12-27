package com.sbi.oem.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class FileUrlResponseDetails {
	public String token;
	public ArrayList<String> fileUrls;

	public FileUrlResponseDetails() {
		super();
	}

	public FileUrlResponseDetails(String token, ArrayList<String> fileUrls) {
		super();
		this.token = token;
		this.fileUrls = fileUrls;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ArrayList<String> getFileUrls() {
		return fileUrls;
	}

	public void setFileUrls(ArrayList<String> fileUrls) {
		this.fileUrls = fileUrls;
	}
}
