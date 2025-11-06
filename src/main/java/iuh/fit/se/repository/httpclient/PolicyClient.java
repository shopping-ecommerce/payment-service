package iuh.fit.se.repository.httpclient;

import iuh.fit.se.dto.response.VersionResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name="policy-service")
public interface PolicyClient {
    @GetMapping("/policies/seller-tos/effective")
    public ResponseEntity<VersionResp> getSellerTosEffective(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate at
    );
}
