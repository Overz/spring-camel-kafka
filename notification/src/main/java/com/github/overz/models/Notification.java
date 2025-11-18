package com.github.overz.models;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Notification implements Serializable {
	private String cdNotification;
	private String cdOrder;
	private LocalDateTime dtCreatedAt = LocalDateTime.now();
}
