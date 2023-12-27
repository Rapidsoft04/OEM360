package com.sbi.oem.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidation {

    public static boolean checkExcelFormat(MultipartFile file) {

        String contentType = file.getContentType();
        if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkPdfFormat(MultipartFile file) {
        String contentType = file.getContentType();


        if (contentType.equalsIgnoreCase("application/pdf")) {
            return true;
        } else {
            return false;
        }
    }

	public static Boolean checkAllFormat(MultipartFile files) {
			String contentType = files.getContentType();
			System.err.println(contentType);

			if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					|| contentType.equals("image/jpeg") || contentType.equals("image/png")
					|| contentType.equals("application/pdf") || contentType.equals("application/txt")
					|| contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")||
					contentType.equals("text/plain")) {
				return true;
			} else {
				return false;
			}
		
	}
}