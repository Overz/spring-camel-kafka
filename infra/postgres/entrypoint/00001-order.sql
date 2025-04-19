CREATE TABLE "order"
(
	cdOrder     TEXT        NOT NULL,
	flStatus    INT         NOT NULL,
	dtCreatedAt TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_order PRIMARY KEY (cdOrder)
);
