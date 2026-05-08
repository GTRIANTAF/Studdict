package com.studdict.repository;

import com.studdict.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    InviteCode findByCode(String code);
}