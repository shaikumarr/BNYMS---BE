// dto/ProjectMembersRequest.java  (NEW)
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProjectMembersRequest(@NotNull List<Long> employeeIds) {
}