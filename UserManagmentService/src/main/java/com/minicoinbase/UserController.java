package com.minicoinbase;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @CookieValue(
                    value = "jwt",
                    defaultValue = "no_jwt"
            ) String jwt_token,
            @RequestBody LoginDto request,
            HttpServletResponse response) {
        Cookie token;
        if(jwt_token.equals("no_jwt")){
            token = userService.login(request.getUsernameOrEmail(), request.getPassword());
            if(token == null){
                return new ResponseEntity<>("Invalid username or password", HttpStatus.BAD_REQUEST);
            }
        }
        else{
            Optional<User> user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail());
            if(user.isEmpty()){
                return new ResponseEntity<>("Invalid username or password", HttpStatus.BAD_REQUEST);
            }
            else {
                if (userService.validateToken(jwt_token) == null) {
                    return new ResponseEntity<>("Already logged in", HttpStatus.OK);
                } else {
                    token = userService.login(request.getUsernameOrEmail(), request.getPassword());
                    if (token == null) {
                        return new ResponseEntity<>("Invalid username or password", HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }
        response.addCookie(token);
        return new ResponseEntity<>(token.getAttribute("jwt"), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto, HttpServletResponse response) {
        if(userRepository.existsByUsername(signUpDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }
        String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if(!signUpDto.getEmail().matches(regexPattern)){
            return new ResponseEntity<>("Email is not valid!", HttpStatus.BAD_REQUEST);
        }
        Cookie cookie = userService.registerUser(signUpDto);
        response.addCookie(cookie);
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(
            value = "jwt",
            defaultValue = "no_jwt"
    ) String jwt_token, HttpServletResponse response){
        if(jwt_token.equals("no_jwt")){
            return new ResponseEntity<>("Not logged in", HttpStatus.BAD_REQUEST);
        }
        userService.logout(jwt_token);
            Cookie cookie = new Cookie("jwt", "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("code") String code){
        if(userService.verifyUser(code)){
            return new ResponseEntity<>("User verified successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid verification code", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/is_verified")
    public ResponseEntity<?> isVerified(@CookieValue(
            value = "jwt",
            defaultValue = "no_jwt"
    ) String jwt_token){
        if(jwt_token.equals("no_jwt")){
            return new ResponseEntity<>("Not logged in", HttpStatus.BAD_REQUEST);
        }
        if(userService.isVerified(jwt_token)){
            return new ResponseEntity<>("User is verified", HttpStatus.OK);
        }
        return new ResponseEntity<>("User is not verified", HttpStatus.BAD_REQUEST);
    }
}