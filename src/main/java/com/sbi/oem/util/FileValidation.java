package com.sbi.oem.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
	
	 public static String getFileExtension(MultipartFile file) {
	        String originalFileName = file.getOriginalFilename();
	        if (originalFileName != null && originalFileName.contains(".")) {
	            return originalFileName.substring(originalFileName.lastIndexOf('.') + 1);
	        }
	        return null; // If the file has no extension or originalFileName is null
	    }
	 
	 public static byte[] convertDocxToPdf(MultipartFile docxFile) {
	        try (InputStream is = docxFile.getInputStream()) {
	            XWPFDocument document = new XWPFDocument(is);

	            // Create options for PDF conversion
	            PdfOptions options = PdfOptions.create();

	            // Customize options if needed
	            // options.set...

	            // Convert the document to PDF
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            PdfConverter.getInstance().convert(document, baos, options);

	            return baos.toByteArray();
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}