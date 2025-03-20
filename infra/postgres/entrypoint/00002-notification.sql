CREATE TABLE "notification"
(
	cdNotification TEXT NOT NULL,
	cdOrder TEXT NOT NULL,
	dtCreatedAt TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_notification PRIMARY KEY (cdNotification)
);
