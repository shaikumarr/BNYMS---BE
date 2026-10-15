// dto/UserResponse.java
package com.mphasis.taskpulse.dto;

import com.mphasis.taskpulse.enums.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}