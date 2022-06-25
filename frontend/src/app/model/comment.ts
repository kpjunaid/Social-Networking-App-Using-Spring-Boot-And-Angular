import { User } from "./user";

export class Comment {
    id: number;
    content: string;
    likeCount: number;
    dateCreated: string;
    dateLastModified: string;
	author: User;
}
