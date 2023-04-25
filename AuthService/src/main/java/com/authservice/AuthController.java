package com.authservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("userId") String userId) {
        String token = jwtUtils.generateToken(userId);
        redisService.save(token, jwtUtils.getTokenExpiration(token) / 1000);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info(String.valueOf(redisService.exists(token)));
        if(!redisService.exists(token)){
            return ResponseEntity.badRequest().body("Token is invalidated");
        }
        if (jwtUtils.validateToken(token)) {
            String newToken = jwtUtils.generateToken(jwtUtils.getUserIdFromToken(token));
            redisService.save(token, jwtUtils.getTokenExpiration(token) / 1000);
            return new ResponseEntity<>(newToken, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        redisService.delete(token);
        return ResponseEntity.ok().body("Token invalidated");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String authHeader, @RequestParam("userId") String userId) {
        String token = authHeader.replace("Bearer ", "");   

        if(!redisService.exists(token)){
            return ResponseEntity.badRequest().body("Invalid token");
        }
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        redisService.delete(token);
        String newToken = jwtUtils.generateToken(userId);
        redisService.save(newToken, jwtUtils.getTokenExpiration(newToken) / 1000);
        return ResponseEntity.ok().body((newToken));
    }

    @GetMapping("/getUserId")
    public ResponseEntity<String> getUserId(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if(!redisService.exists(token)){
            return ResponseEntity.badRequest().body("Token is invalidated");
        }
        if (jwtUtils.validateToken(token)) {
            String userId = jwtUtils.getUserIdFromToken(token);
            return new ResponseEntity<>(userId, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
