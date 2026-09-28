package com.example.walletservice.repo;

import com.example.walletservice.domain.Outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepo extends JpaRepository<Outbox, UUID> {


    @Query(value = "SELECT o.* from outbox as o where o.status = false ORDER BY o.id ASC LIMIT 100 FOR UPDATE SKIP LOCKED ",nativeQuery = true)
    List<Outbox> findAllByStatusIsFalse();

    @Modifying
    @Query("DELETE FROM Outbox o WHERE o.id IN :ids")
    void deleteByIdsInBatch(@Param("ids") Collection<UUID> ids);



}
