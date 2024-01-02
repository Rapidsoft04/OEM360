package com.sbi.oem.util;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.repository.CredentialMasterRepository;

/**
 * This class provides utility function for sending HTTP POST, PATCH, DELETE
 * request to a different endpoint
 */
@Component
@SuppressWarnings("unused")
public class Utility {

	@Autowired
	private CredentialMasterRepository credentialMasterRepository;

	private static int responseCount(com.squareup.okhttp.Response response) {
		int result = 1;
		while ((response = response.priorResponse()) != null) {
			result++;
		}
		return result;
	}

	public static HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
			}
		};
	}

	public boolean hasPermission(String url, String methodType, String username) {

		boolean hasPermission = false;
		try {

			Optional<CredentialMaster> credentialMasterOptional = credentialMasterRepository.findByEmail(username);
			if (credentialMasterOptional.isPresent()) {
				String userType = credentialMasterOptional.get().getUserTypeId().name();
				if (userType.equalsIgnoreCase(UserType.OEM_SI.name())) {
					return true;
				} else {
					if (userType.equalsIgnoreCase(UserType.APPLICATION_OWNER.name())) {
						return true;
					} else {
						if (userType.equalsIgnoreCase(UserType.AGM.name())) {
							return true;
						}
						else if (userType.equalsIgnoreCase(UserType.GM_IT_INFRA.name())) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasPermission;
	}
}