package io.envio.auth.domain.cli.repository;

import org.springframework.data.repository.CrudRepository;

import io.envio.auth.domain.cli.entity.RedisCliSession;

public interface RedisCliSessionRepository extends CrudRepository<RedisCliSession, String> {
}