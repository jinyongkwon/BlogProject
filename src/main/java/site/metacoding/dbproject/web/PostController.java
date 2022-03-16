package site.metacoding.dbproject.web;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.post.Post;
import site.metacoding.dbproject.domain.post.PostRepository;
import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.service.PostService;

@RequiredArgsConstructor // final이 붙은 애들에 대한 생성자를 만들어준다.
@Controller
public class PostController {

    private final HttpSession session;
    private final PostService postService;

    // GET 글쓰기 페이지 /post/writeForm - 인증 O
    @GetMapping("/s/post/writeForm")
    public String writeForm() {
        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }
        return "post/writeForm";
    }

    // 메인페이지 - 인증 X
    // GET 글목록 페이지 /post/list, /
    // @GetMapping({"/", "/post/list"})
    @GetMapping({ "/", "/post/list" })
    public String list(@RequestParam(defaultValue = "0") Integer page, Model model) {
        Page<Post> pagePosts = postService.글목록보기(page);
        model.addAttribute("posts", pagePosts);
        model.addAttribute("prevPage", page - 1);
        model.addAttribute("nextPage", page + 1);
        return "post/list";
    }

    // GET 글상세보기 페이지 /post/{id} (삭제버튼 만들어 두면됨, 수정버튼 만들어 두면됨) - 인증 X
    @GetMapping("/post/{id}") // Get요청에 /post 제외 시키기
    public String detail(@PathVariable Integer id, Model model) {
        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {// 박스안에 값이 있으면 = 선물!!
            Post postEntity = postOp.get(); // 선물이있으면 값을 넣음.
            model.addAttribute("post", postEntity);
            return "post/detail";
        } else {
            return "error/page1"; // DB를 억지로 날리지 않는이상 여기로 올수가없음.
        }
    }

    // GET 글수정 페이지 /post/{id}/updateForm - 인증 O
    @GetMapping("/s/post/{id}/updateForm")
    public String updateForm(@PathVariable Integer id) {
        return "post/updateForm"; // ViewResolver 도움 받음.
    }

    // DELETE 글삭제 /post/{id} - 글목록으로 가기 - 인증 O
    @DeleteMapping("/s/post/{id}")
    public String delete(@PathVariable Integer id) {
        return "redirect:/";
    }

    // UPDATE 글수정 /post/{id} - 글상세보기 페이지가기 - 인증 O
    @PutMapping("/s/post/{id}")
    public String update(@PathVariable Integer id) {
        return "redirect:/post/" + id;
    }

    // POST 글쓰기 /post - 글목록으로 가기 - 인증 O
    @PostMapping("/s/post")
    public String write(Post post) {
        // title, content 1.null검사, 2. 공백검사, 3.길이검사 .......
        if (session.getAttribute("principal") == null) {
            return "redirect:/loginForm";
        }
        User principal = (User) session.getAttribute("principal");
        post.setUser(principal); // user의 id만 들어감
        // insert into post(title, content, userid) values(사용자, 사용자, 세션오브젝트의 PK)
        postRepository.save(post);
        return "redirect:/";
    }
}