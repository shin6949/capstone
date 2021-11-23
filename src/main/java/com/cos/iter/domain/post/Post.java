package com.cos.iter.domain.post;

import com.cos.iter.domain.comment.Comment;
import com.cos.iter.domain.image.Image;
import com.cos.iter.domain.like.Like;
import com.cos.iter.domain.tag.Tag;
import com.cos.iter.domain.user.User;
import com.cos.iter.web.dto.UserProfilePostRespDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.istack.NotNull;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SqlResultSetMapping(
        name = "UserProfilePostRespDtoMapping",
        classes = @ConstructorResult(
                targetClass = UserProfilePostRespDto.class,
                columns = {
                        @ColumnResult(name="id", type = Integer.class),
                        @ColumnResult(name="image_url", type=String.class),
                        @ColumnResult(name="like_count", type = Integer.class),
                        @ColumnResult(name="comment_count", type = Integer.class)
                }
        )
)
@Entity(name = "post")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false, exclude = {"tags", "images", "comments", "likes"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createDate;

    @NotNull
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name="FK_POST_USER_ID"))
    private User user;

    private String mapImageUrl;

    // Image를 select하면 여러개의 Tag가 딸려옴.
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE) // 연관관계 주인의 변수명을 적는다.
    @JsonIgnoreProperties({"post"})
    @ToString.Exclude // Jackson한테 내리는 명령
    private List<Tag> tags;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties({"post"})
    @ToString.Exclude // Jackson한테 내리는 명령
    private List<Image> images;

    @JsonIgnoreProperties({"post"})
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Comment> comments;

    @JsonIgnoreProperties({"post"})
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Like> likes;

    @NotNull
    @ColumnDefault("0")
    private int viewCount;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("true")
    private Boolean visible;

    @Transient
    private boolean postHost;

    @Transient
    private int likeCount;

    @Transient
    private int commentCount;

    @Transient
    private int popularRate;

    @Transient
    private boolean likeState;

    @Transient
    @ToString.Exclude
    private Image firstImage;

    public String getUserProfileImage() {
        return user.getProfileImage();
    }

    public String getCreateDateString() {
        return getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
    }

    public Image getFirstImage() {
        return getImages().get(0);
    }

    public String toJavaScriptData() {
        StringBuilder result = new StringBuilder("[");

        for(int i = 0; i < images.size(); i++) {
            final Image image = images.get(i);

            result.append("{\"place_name\": \"").append(image.getLocationName()).append("\",");
            result.append("\"place_url\": \"").append(image.getKakaoMapUrl()).append("\",");
            if(image.getRoadAddress() != null) {
                result.append("\"road_address_name\": \"").append(image.getRoadAddress()).append("\",");
            }
            result.append("\"x\": \"").append(image.getLongitude()).append("\",");
            result.append("\"y\": \"").append(image.getLatitude()).append("\"}");

            if(i != (images.size() -1 )) {
                result.append(",");
            }
        }

        result.append("];");

        log.info("Converted Data: " + result);
        return result.toString();
    }
}
