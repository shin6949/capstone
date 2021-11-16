package com.cos.iter.domain.image;

import javax.persistence.*;

import com.cos.iter.domain.post.Post;

import lombok.*;

@Entity(name = "image")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, exclude = {"post"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String url;
	private float latitude;
	private float longitude;
	private String locationName;
	private String roadAddress;
	private String kakaoMapUrl;

	@ManyToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name="post_id", foreignKey = @ForeignKey(name="FK_IMAGE_POST_ID"))
	private Post post;

	@Column(columnDefinition = "smallint default 0")
	private short sequence;

	public String getUrl() {
		final String blogStorageUrl = System.getenv("AZURE_BLOB_URL");

		return blogStorageUrl + "/photo/" + url;
	}
}





