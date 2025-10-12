package com.tushar.geotrackr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification {
    private String type; // INFO, WARNING, ERROR
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private Object data; // Additional data
}
