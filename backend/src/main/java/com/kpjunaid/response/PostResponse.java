package com.kpjunaid.response;

import com.kpjunaid.entity.Post;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Post post;
    private Boolean likedByAuthUser;
}
