package com.tujahelper.brokerage.repository

import com.tujahelper.brokerage.domain.BrokerageCredential
import org.springframework.data.jpa.repository.JpaRepository

interface BrokerageCredentialRepository : JpaRepository<BrokerageCredential, Long> {
    fun findByUserId(userId: Long): BrokerageCredential?
}
