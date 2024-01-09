package com.sbi.oem.serviceImpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.sbi.oem.dto.FileUrlResponse;

@Service
public class FileSystemStorageService {


	@Value("${fileAccessUrl}")
	private String fileAccessUrl;
	@Value("${imageUrlToken}")
	private String imageUrlToken;





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