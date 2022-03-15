package site.metacoding.dbproject.web;

import java.util.Optional;

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

import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.domain.user.UserRepository;

@Controller
public class UserController {

    // 컴퍼지션 (의존성 연결)
    private UserRepository userRepository;
    private HttpSession session;

    // DI 받는 코드. // @Autowired와 같음.
    public UserController(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
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
        // 1.1 null체크
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return "redirect:/joinForm"; // 새로고침 되기 때문에 최악 => 뒤로가기 해주면 해결
        }
        // 1.2 공백체크
        if (user.getUsername().equals("") || user.getPassword().equals("") || user.getEmail().equals("")) {
            return "redirect:/joinForm";
        }
        // 2. 핵심로직
        User userentity = userRepository.save(user);
        System.out.println("userentity : " + userentity);
        // redirect : 매핑주소
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

        // 1. DB연결해서 username, password 있는지 확인
        User userEntity = userRepository.mLogin(user.getUsername(), user.getPassword());

        // 2. 있으면 session 영역에 인증됨 이라고 메시지 하나 넣어두자.
        if (userEntity == null) {
            System.out.println("아이디 혹은 패스워드가 틀렸습니다.");
        } else {
            System.out.println("로그인 되었습니다.");
            session.setAttribute("principal", userEntity); // session에 user의 정보를 기록!!

            if (user.getRemember() != null) {
                if (user.getRemember().equals("on")) {
                    response.addHeader("Set-Cookie", "remember=" + userEntity.getUsername());
                }
            }
        }

        return "redirect:/"; // PostController 만들고 수정하자.
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

        // 3. 핵심로직
        Optional<User> userOp = userRepository.findById(id);

        if (userOp.isPresent()) {// 박스안에 값이 있으면 = 선물!!
            User userEntity = userOp.get(); // 선물이있으면 값을 넣음.
            model.addAttribute("user", userEntity);
            return "user/detail";
        } else {
            return "error/page1"; // DB를 억지로 날리지 않는이상 여기로 올수가없음.
        }
    }

    // 유저수정 페이지 (동적) - 로그인 O
    @GetMapping("/s/user/updateForm")
    public String updateForm() {
        return "user/updateForm";
    }

    // 유저수정 - 로그인 O
    @PutMapping("/s/user/{id}")
    public String update(@PathVariable Integer id) {
        return "redirect:/user/" + id;
    }

}
