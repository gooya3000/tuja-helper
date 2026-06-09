package com.tujahelper.kis.client

import com.tujahelper.account.dto.BalanceDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.math.BigDecimal

data class KisBalanceResponse(
    val output2: List<KisBalanceSummary>? = null,
)

data class KisBalanceSummary(
    val tot_evlu_amt: String? = null,       // 총평가금액
    val nxdy_excc_amt: String? = null,      // 익일정산금액(예수금)
    val evlu_pfls_smtl_amt: String? = null, // 평가손익합계금액
    val evlu_erng_rt: String? = null,       // 평가수익율
)

@Component
class KisApiClient(
    @Value("\${app.kis.base-url}") private val baseUrl: String,
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    fun getBalance(accessToken: String, appKey: String, appSecret: String, accountNo: String): BalanceDto {
        // 계좌번호는 앞 8자리(CANO) + 뒷 2자리(ACNT_PRDT_CD)로 분리
        val cano = accountNo.take(8)
        val acntPrdtCd = accountNo.drop(8).take(2)

        val response = webClient.get()
            .uri { builder ->
                builder.path("/uapi/domestic-stock/v1/trading/inquire-balance")
                    .queryParam("CANO", cano)
                    .queryParam("ACNT_PRDT_CD", acntPrdtCd)
                    .queryParam("AFHR_FLPR_YN", "N")
                    .queryParam("OFL_YN", "")
                    .queryParam("INQR_DVSN", "02")
                    .queryParam("UNPR_DVSN", "01")
                    .queryParam("FUND_STTL_ICLD_YN", "N")
                    .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                    .queryParam("PRCS_DVSN", "00")
                    .queryParam("CTX_AREA_FK100", "")
                    .queryParam("CTX_AREA_NK100", "")
                    .build()
            }
            .header("Authorization", "Bearer $accessToken")
            .header("appkey", appKey)
            .header("appsecret", appSecret)
            .header("tr_id", "VTTC8434R") // 모의투자용 TR ID
            .retrieve()
            .bodyToMono<KisBalanceResponse>()
            .block()

        val summary = response?.output2?.firstOrNull()
        return BalanceDto(
            totalEvaluationAmount = summary?.tot_evlu_amt?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            depositAmount = summary?.nxdy_excc_amt?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            totalProfitLossAmount = summary?.evlu_pfls_smtl_amt?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            totalProfitLossRate = summary?.evlu_erng_rt?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
        )
    }
}
