// dto/LoginResponse.java
package com.mphasis.taskpulse.dto;

import com.mphasis.taskpulse.enums.Role;

public record LoginResponse(String token, Long id, String name, String email, Role role) {
}