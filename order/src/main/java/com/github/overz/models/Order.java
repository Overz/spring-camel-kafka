package com.github.overz.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = Order.TABLE)
public class Order implements Serializable {
	public static final String TABLE = "\"order\"";

	@Id
	@Column(name = Fields.cdOrder, nullable = false)
	private String cdOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = Fields.flStatus, nullable = false)
	@Builder.Default
	private OrderStatus flStatus = OrderStatus.NEW;

	@Column(name = Fields.dtCreatedAt, nullable = false)
	@Builder.Default
	private OffsetDateTime dtCreatedAt = OffsetDateTime.now();

	@Column(name = Fields.dtUpdatedAt)
	private OffsetDateTime dtUpdatedAt;

	@Column(name = Fields.data, nullable = false)
	private String data;

	@PrePersist
	public void prePersist() {
		switch (this.flStatus) {
			case NEW -> {
				this.cdOrder = UUID.randomUUID().toString();
				this.dtUpdatedAt = null;
			}
			case PROCESSING, COMPLETED -> this.dtUpdatedAt = OffsetDateTime.now();
		}
	}
}