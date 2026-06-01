package com.jvmdevelop.strife.repo;

import com.jvmdevelop.strife.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepo extends JpaRepository<Chat, Long> {
    List<Chat> findByIsTetATetTrueAndUsers_Id(Long userId);

    @Query("SELECT DISTINCT c FROM Chat c JOIN c.users u WHERE u.id = :userId")
    List<Chat> findAllByUserId(@Param("userId") Long userId);
}
