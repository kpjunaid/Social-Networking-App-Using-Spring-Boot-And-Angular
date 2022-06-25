package com.kpjunaid.response;

import com.kpjunaid.entity.Comment;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Comment comment;
    private Boolean likedByAuthUser;
}
