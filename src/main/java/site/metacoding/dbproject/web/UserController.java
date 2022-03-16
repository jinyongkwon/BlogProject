package site.metacoding.dbproject.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.service.UserService;
import site.metacoding.dbproject.web.dto.ResponseDto;

@RequiredArgsConstructor
@Controller
public class UserController {

    // 컴퍼지션 (의존성 연결)
    private final UserService userService;
    private final HttpSession session;

    // http://localhost:8080/api/user/username/same-check?username=s
    // user의 username이 동일한지 확인해줄래? - 응답 (JSON)
    @GetMapping("/api/user/username/same-check")
    public @ResponseBody ResponseDto<String> sameCheck(String username) {
        String data = userService.유저네임중복검사(username);
        return new ResponseDto<String>(1, "통신성공", data);
    }

    // 회원가입 페이지 (정적) - 로그인 X
    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    // username=ssar&password=1234&email=ssar@nate.com (x-www-form-urlencoded)
    // 회원가입 - 로그인 X
    @PostMapping("/join")
    public String join(User user) { // 기본기를 위해 먼저 잡아보고 후에 try-catch로 잡는다.
        // 필터의 역활
        // 1.1 null체크
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return "redirect:/joinForm"; // 새로고침 되기 때문에 최악 => 뒤로가기 해주면 해결
        }
        // 1.2 공백체크
        if (user.getUsername().equals("") || user.getPassword().equals("") || user.getEmail().equals("")) {
            return "redirect:/joinForm";
        }
        userService.회원가입(user);
        return "redirect:/loginForm"; // 로그인페이지 이동해주는 컨트롤러 메서드를 재활용
    }

    // 로그인 페이지 (정적) - 로그인 X
    @GetMapping("/loginForm")
    public String loginForm(HttpServletRequest request, Model model) {
        // JSESSIONID=asidaisdjasdi1233;remember=ssar
        // request.getHeader("Cookie");
        if (request.getCookies() != null) {
            Cookie[] cookies = request.getCookies(); // JSESSIONID,remember 2개가 있음. 내부적으로 split해준것.
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("remember")) {
                    model.addAttribute("remember", cookie.getValue());
                }
            }
        }
        return "user/loginForm";
    }

    // SELECT * From user WHERE username=? AND password=?
    // 원래 SELET는 무조건 Get요청
    // 그런데 로그인만 예외 (POST)
    // 이유 : 주소에 패스워드를 남길 수 없으니까!! => 보안을 위해 POST를 사용!!
    // 로그인 - 로그인 X
    @PostMapping("/login")
    public String login(User user, HttpServletResponse response) {

        User userEntity = userService.로그인(user);

        if (userEntity != null) {
            session.setAttribute("principal", userEntity); // session에 user의 정보를 기록!!
            if (user.getRemember() != null && user.getRemember().equals("on")) {
                response.addHeader("Set-Cookie", "remember=" + userEntity.getUsername());
            }
            return "redirect:/";
        } else {
            return "redirect:/loginForm"; // 자바스크립트로 history back 해주는게 좋음.
        }

    }

    // 로그아웃 - 로그인 O
    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 세션의 모든영역을 날림.
        return "redirect:/loginForm"; // PostController 만들고 수정하자.
    }

    // http://localhost:8080/user/1
    // 유저상세 페이지 (동적) - 로그인 O
    @GetMapping("/s/user/{id}")
    public String detail(@PathVariable Integer id, Model model) {

        // 유효성 검사하기 (엄청나게 많음.)
        User principal = (User) session.getAttribute("principal");

        // 1. 인증 체크
        if (principal == null) {
            return "error/page1";
        }

        // 2. 권한체크
        if (principal.getId() != id) {
            return "error/page1";
        }

        User userEntity = userService.유저정보보기(id);
        if (userEntity == null) {
            return "error/page1";
        } else {
            model.addAttribute("user", userEntity);
            return "user/detail";
        }
    }

    // 유저수정 페이지 (동적) - 로그인 O
    @GetMapping("/s/user/updateForm")
    public String updateForm() {
        // 세션값을 출력했는데, 원래는 DB에서 가져와야 함.
        return "user/updateForm";
    }

    // 유저수정 - 로그인 O
    @PutMapping("/s/user/{id}")
    public String update(@PathVariable Integer id) {
        return "redirect:/user/" + id;
    }

}
