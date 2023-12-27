package com.sbi.oem.security;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.repository.CredentialMasterRepository;


@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private CredentialMasterRepository credentialMasterRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        org.springframework.security.core.userdetails.User user = null;
        try {
            Optional<CredentialMaster> credentialMasterOptional = credentialMasterRepository.findByEmail(username);

            if (credentialMasterOptional.isPresent()) {
                Collection<UserType> authorities = new ArrayList<>();
                authorities.add(credentialMasterOptional.get().getUserTypeId());
                user = new org.springframework.security.core.userdetails.User(credentialMasterOptional.get().getEmail(), credentialMasterOptional.get().getPassword(),
                        getAuthorities(authorities));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<UserType> roles) {
        List<GrantedAuthority> authorities
                = new ArrayList<>();
        for (UserType role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        return authorities;
    }
    
    public Optional<CredentialMaster> getUserDetails() {
    	try {
    		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    		Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
    		return master;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    	
    }

}
