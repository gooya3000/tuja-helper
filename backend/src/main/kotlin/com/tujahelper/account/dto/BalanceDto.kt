package com.tujahelper.account.dto

import java.math.BigDecimal

data class BalanceDto(
    val totalEvaluationAmount: BigDecimal,
    val depositAmount: BigDecimal,
    val totalProfitLossAmount: BigDecimal,
    val totalProfitLossRate: BigDecimal,
)
