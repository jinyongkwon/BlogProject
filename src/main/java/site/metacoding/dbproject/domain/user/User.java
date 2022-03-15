package site.metacoding.dbproject.domain.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;

// JPA 라이브러리는 Java Persistence(DB에 영구적인 저장) API(노출되어 있는 메서드)
// => Java언어로 DB에 영구적으로 저장할수 있는 메서드를 제공해주는 라이브러리
// 1. CRUD 메서드를 기본 제공
// 2. 자바코드로 DB를 자동 생성 기능 제공 - 설정이 필요!!
// 3. ORM 제공!! - 이 부분 지금 몰라도 됨. (Object Relaction)
@AllArgsConstructor
@NoArgsConstructor
@Data // Getter, Setter, ToString
@Entity // 서버 실행시 해당 클래스로 테이블을 생성해!!
@EntityListeners(AuditingEntityListener.class) // 현재시간 입력을 위해 필요한 어노테이션
public class User {
    // IDENTITY 전략은 DB에게 번호증가 정략을 위힘하는 것!! - 알아서 DB에 맞게 찾아줌
    @Id // Primary Key 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Primary Key

    @Column(length = 20, unique = true) // 길이 20, 유니크 제약(중복 x) 검
    private String username; // ssar 아이디
    @Column(length = 12, nullable = false) // 길이 12, not null
    private String password;
    @Column(length = 16000000, nullable = false) // 1600만 byte => 자동으로 longtext로 전환
    private String email;
    @CreatedDate // insert
    private LocalDateTime createDate; // Java에서는 LocalDateTime이 있는데 DB에서 자동으로 datetime으로 자동 전환!!
    // 근데 단점은 자동으로 커멜표기법은 언더스크롤로 바뀜. => 설정파일에서 바꾸기 가능.
    @LastModifiedDate // insert, update
    private LocalDateTime updateDate;

    ///////////////////////////////////////////////// DB테이블과 상관없음
    @Transient // DB에 칼럼 만들지마라는 어노테이션
    private String remember;

}
