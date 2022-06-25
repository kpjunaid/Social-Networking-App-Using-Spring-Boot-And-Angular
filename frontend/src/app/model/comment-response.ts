import { Comment } from "./comment";

export class CommentResponse {
	likedByAuthUser: boolean;
	comment: Comment;
}
