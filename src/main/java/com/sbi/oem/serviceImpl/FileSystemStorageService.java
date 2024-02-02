package com.sbi.oem.serviceImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.sbi.oem.config.FileUploadProperties;
import com.sbi.oem.dto.FileUrlResponse;

@Service
public class FileSystemStorageService {

	private final Path fileStorageLocation;
	
	@Value("${fileAccessUrl}")
	private String fileAccessUrl;
	@Value("${imageUrlToken}")
	private String imageUrlToken;

	@Value("${file.upload-dir}")
	private String filePath;

	

	@Autowired
    public FileSystemStorageService(FileUploadProperties fileStorageProperties) {
	        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
	                .toAbsolutePath().normalize();

	        try {
	            Files.createDirectories(this.fileStorageLocation);
	        } catch (Exception ex) {
	            //throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
	        }
	    }

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {

			throw new RuntimeException("Could not create upload dir!");
		}

	}

	public String storeFile(MultipartFile file, Long currentDate) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(currentDate + file.getOriginalFilename());
		try {
			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return fileName;
		} catch (IOException ex) {
			throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!",
					ex);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new RuntimeException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new RuntimeException("File not found " + fileName, ex);
		}
	}

	public Path searchFile(String fileName) {
		try {
			System.out.println(filePath);
			Path baseDirectory = Paths.get(filePath);
			Path filePath = Files.walk(baseDirectory).filter(p -> p.getFileName().toString().equals(fileName))
					.findFirst().orElseThrow(() -> new FileNotFoundException("File not found"));

			System.out.println("File found: " + filePath);
			return filePath;
		} catch (IOException e) {
			System.err.println("An error occurred: " + e.getMessage());
		}
		return null;
	}

	public String getUserExpenseFileUrl(MultipartFile file) {
		String fileUrl = null;
		try {
			if (file != null && file.getBytes().length > 0) {
				fileUrl = getFileUrl(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl;
	}

	private String getFileUrl(MultipartFile file) {
		String fileUrl = null;
		try {
			String url = fileAccessUrl;
			RestTemplate restTemplate = new RestTemplate(); // create headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			LinkedMultiValueMap<Object, Object> map = new LinkedMultiValueMap<>();
			map.add("files", new MultipartInputStreamFileResources(file.getInputStream(), file.getOriginalFilename()));
			ResponseEntity<String> response = null;
			String responseBody = null;
			FileUrlResponse responseBodyDto = new FileUrlResponse();
			if (imageUrlToken != null && !imageUrlToken.isEmpty() && file != null && !file.isEmpty()) {
				try {
					map.add("token", imageUrlToken);
					HttpEntity<LinkedMultiValueMap<Object, Object>> entity = new HttpEntity<>(map, headers);
					response = restTemplate.postForEntity(url, entity, String.class);
					responseBody = response.getBody();
					responseBodyDto = new Gson().fromJson(responseBody, responseBodyDto.getClass());
					if (responseBodyDto.getResponseCode().equals(200) && responseBodyDto.getData() != null
							&& responseBodyDto.getData().getFileUrls() != null
							&& !responseBodyDto.getData().getFileUrls().isEmpty()) {
						fileUrl = responseBodyDto.getData().getFileUrls().get(0);
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return fileUrl;
	}

}