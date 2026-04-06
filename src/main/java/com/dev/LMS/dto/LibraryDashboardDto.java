package com.dev.LMS.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryDashboardDto {
    private long totalResources;
    private long availableResources;
    private long totalBorrows;
    private long activeBorrows;
    private long myBorrows;
    private long myFavorites;
}
