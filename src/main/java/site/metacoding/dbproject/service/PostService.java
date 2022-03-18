package site.metacoding.dbproject.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.dbproject.domain.post.Post;
import site.metacoding.dbproject.domain.user.User;
import site.metacoding.dbproject.domain.post.PostRepository;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> 글목록보기(Integer page) {
        PageRequest pq = PageRequest.of(page, 3, Sort.by(Direction.DESC, "id"));
        return postRepository.findAll(pq);
    }

    // 글상세보기, 글수정페이지
    public Post 글상세보기(Integer id) {
        Optional<Post> postOp = postRepository.findById(id);
        // 조회수 증가 update()를 추가하려면 @Transactional을 걸어주어야한다.

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();
            return postEntity;
        } else {
            return null;
        }
    }

    @Transactional
    public void 글수정하기(Post post, Integer id) {
        Optional<Post> postOP = postRepository.findById(id);
        if(postOP.isPresent()){
            Post postEntity = postOP.get();
            postEntity.setTitle(post.getTitle());
            postEntity.setContent(post.getContent());
        } // 더티체킹 완료 (수정됨)
    }

    @Transactional
    public void 글삭제하기(Integer id) {
        postRepository.deleteById(id); // 실패시 내부적으로 exception 터짐
    }

    @Transactional
    public void 글쓰기하기(Post post, User principal) {
        post.setUser(principal); // User FK 추가!!
        postRepository.save(post);
    }
}
