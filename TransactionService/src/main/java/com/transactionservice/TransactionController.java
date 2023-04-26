package com.transactionservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/transaction")
public class TransactionController
{
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RestTemplate restTemplate;
    @PostMapping("/buy")
    public ResponseEntity<?> buyCrypto(@CookieValue(value = "jwt", defaultValue = "no_jwt") String jwt_token, @RequestBody TransactionDto transactionDto)
    {
        try
        {
            if(jwt_token.equals("no_jwt")){
                return ResponseEntity.badRequest().body("Not logged in");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt_token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange("http://auth-service:8090/api/auth/getUser", org.springframework.http.HttpMethod.GET, requestEntity, String.class);
            if(responseEntity.getStatusCode().isError()){
                return ResponseEntity.badRequest().body("Invalid token");
            }
            String userId = responseEntity.getBody();
            assert userId != null;
            transactionDto.setUserId(Long.valueOf(userId));
            transactionService.buyCrypto(transactionDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellCrypto(@CookieValue(value = "jwt", defaultValue = "no_jwt") String jwt_token, @RequestBody TransactionDto transactionDto)
    {
        try
        {
            if(jwt_token.equals("no_jwt")){
                return ResponseEntity.badRequest().body("Not logged in");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt_token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange("http://auth-service:8090/api/auth/getUser", org.springframework.http.HttpMethod.GET, requestEntity, String.class);
            if(responseEntity.getStatusCode().isError()){
                return ResponseEntity.badRequest().body("Invalid token");
            }
            String userId = responseEntity.getBody();
            assert userId != null;
            transactionDto.setUserId(Long.valueOf(userId));
            transactionService.sellCrypto(transactionDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}