import { User } from "./user";

export class UserResponse {
    user: User;
    followedByAuthUser: boolean;
}