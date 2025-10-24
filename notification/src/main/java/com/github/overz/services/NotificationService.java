package com.github.overz.services;

import com.github.overz.models.Notification;
import com.github.overz.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	public static final String CACHE_NAME = "order-cache";

	private final NotificationRepository repository;

	@Transactional
	public Notification save(final String orderId) {
		final var entity = Notification.builder()
			.cdNotification(orderId) // use order id as notification id for simplicity
			.cdOrder(orderId)
			.build();
		return repository.save(entity);
	}

	@Cacheable(cacheNames = CACHE_NAME, key = "#orderId")
	public Optional<Notification> findByOrderIdCached(final String orderId) {
		log.debug("[CACHE] miss for orderId='{}' - querying database", orderId);
		return repository.findByCdOrder(orderId);
	}

	@CacheEvict(cacheNames = CACHE_NAME, key = "#orderId")
	public void evictCache(final String orderId) {
		log.debug("[CACHE] evicted orderId='{}'", orderId);
	}
}
