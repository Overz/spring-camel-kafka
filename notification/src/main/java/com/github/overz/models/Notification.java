package com.github.overz.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = Notification.TABLE)
public class Notification implements Serializable {
	public static final String TABLE = "\"notification\"";

	@Id
	@Column(name = Fields.cdNotification, nullable = false)
	private String cdNotification;

	@Column(name = Fields.cdOrder, nullable = false)
	private String cdOrder;

	@Column(name = Fields.dtCreatedAt, nullable = false)
	@Builder.Default
	private OffsetDateTime dtCreatedAt = OffsetDateTime.now();
}
