CREATE TABLE "order"
(
	cdOrder     TEXT        NOT NULL,
	flStatus    TEXT        NOT NULL,
	dtCreatedAt TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
	dtUpdatedAt TIMESTAMPTZ NULL,
	data        TEXT        NOT NULL,

	CONSTRAINT fk_order PRIMARY KEY (cdOrder)
);
