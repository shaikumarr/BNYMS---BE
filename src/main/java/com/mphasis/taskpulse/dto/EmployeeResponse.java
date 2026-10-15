// dto/EmployeeResponse.java
package com.mphasis.taskpulse.dto;

import java.util.List;

public record EmployeeResponse(Long id, String name, String email, List<ProjectSummary> projects) {
}