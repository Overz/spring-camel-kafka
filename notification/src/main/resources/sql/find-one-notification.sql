SELECT cdnotification as cdNotification,
			 cdorder        as cdOrder,
			 dtcreatedat    as dtCreatedAt
FROM "notification"
WHERE cdorder = :#${exchangeProperty.cdOrder}