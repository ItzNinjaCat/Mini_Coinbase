package com.authservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader("Authorization") String authHeader, @RequestParam("userId") String userId) {
        String token = authHeader.replace("Bearer ", "");
        if(redisService.isBlacklisted(token)){
            return ResponseEntity.badRequest().body("Token is invalidated");
        }
        if (jwtUtils.validateToken(token, userId)) {
            String newToken = jwtUtils.generateToken(userId);
            return new ResponseEntity<>(newToken, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        redisService.addToBlacklist(token);
        return ResponseEntity.ok().body("Token invalidated");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String authHeader, @RequestParam("userId") String userId) {
        String token = authHeader.replace("Bearer ", "");   

        if(redisService.isBlacklisted(token)){
            return ResponseEntity.badRequest().body("Invalid token");
        }
        if (!jwtUtils.validateToken(token, userId)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        String newToken = jwtUtils.generateToken(userId);

        return ResponseEntity.ok().body((newToken));
    }
}
