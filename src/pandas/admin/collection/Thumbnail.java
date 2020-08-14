package pandas.admin.collection;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Thumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "THUMBNAIL_SEQ")
    private Long id;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;

    private String sourceUrl;
    private String sourceType;

    private Integer width;
    private Integer height;
    private Integer clipX;
    private Integer clipY;
    private Integer clipWidth;
    private Integer clipHeight;

    private String contentType;

    private byte[] data;
}
