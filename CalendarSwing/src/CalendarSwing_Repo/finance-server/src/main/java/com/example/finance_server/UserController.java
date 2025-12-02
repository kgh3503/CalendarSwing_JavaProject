package com.example.finance_server; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // @Autowired: 스프링이 3-1에서 만든 UserRepository를 여기에 자동으로 연결
    @Autowired
    private UserRepository userRepository;

    /**
     * LoginView의 '로그인' 버튼이 호출할 API
     * * @RequestBody Map<String, String> loginRequest
     * : 클라이언트가 보낸 JSON (예: {"username":"id", "password":"pw"})을 Map으로 받음
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // DB에서 사용자 조회
        User user = userRepository.findByUsername(username);

        // LoginView의 handleLogin()에 있던 로직을 서버에서 수행
        // (주의: 임시 해시 검증 로직을 그대로 사용)
        if (user != null && user.getPasswordHash().equals(password + "_hashed")) {
            // 로그인 성공: User 객체를 JSON으로 변환하여 클라이언트에 전송 (HTTP 200)
            return ResponseEntity.ok(user);
        } else {
            // 로그인 실패: '권한 없음' 상태 전송 (HTTP 401)
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * LoginView의 '회원가입' 버튼이 호출할 API.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");

        try {
            // 1. LoginView의 handleSignup()에 있던 로직을 서버에서 수행
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPasswordHash(password + "_hashed"); // (임시 해시)

            // 2. 3-1에서 만든 Repository로 DB에 저장
            User savedUser = userRepository.save(newUser);

            // 3. 회원가입 성공: 생성된 User 객체를 전송 (HTTP 200)
            return ResponseEntity.ok(savedUser);

        } catch (DataIntegrityViolationException e) {
            // 4. 회원가입 실패 (아이디 중복 등 DB 제약조건 위반)
            // 충돌 상태 전송 (HTTP 409)
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            // 5. 기타 서버 내부 오류
            return ResponseEntity.status(500).build();
        }
    }
}