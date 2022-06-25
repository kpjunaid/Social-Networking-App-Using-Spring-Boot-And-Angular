import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Post } from '../model/post';
import { PostResponse } from '../model/post-response';
import { ResetPassword } from '../model/reset-password';
import { UpdateUserEmail } from '../model/update-user-email';
import { UpdateUserInfo } from '../model/update-user-info';
import { UpdateUserPassword } from '../model/update-user-password';
import { User } from '../model/user';
import { UserResponse } from '../model/user-response';

@Injectable({
	providedIn: 'root'
})
export class UserService {
	private host = environment.apiUrl;
	private jwtService = new JwtHelperService();

	constructor(private httpClient: HttpClient) { }

	getUserById(userId: number): Observable<UserResponse | HttpErrorResponse> {
		return this.httpClient.get<UserResponse | HttpErrorResponse>(`${this.host}/users/${userId}`);
	}

	getUserFollowingList(userId: number, page: number, size: number): Observable<UserResponse[] | HttpErrorResponse> {
		const reqParams = new HttpParams().set('page', page).set('size', size);
		return this.httpClient.get<UserResponse[] | HttpErrorResponse>(`${this.host}/users/${userId}/following`, { params: reqParams });
	}

	getUserFollowerList(userId: number, page: number, size: number): Observable<UserResponse[] | HttpErrorResponse> {
		const reqParams = new HttpParams().set('page', page).set('size', size);
		return this.httpClient.get<UserResponse[] | HttpErrorResponse>(`${this.host}/users/${userId}/follower`, { params: reqParams });
	}

	getUserPosts(userId: number, page: number, size: number): Observable<PostResponse[] | HttpErrorResponse> {
		const reqParams = new HttpParams().set('page', page).set('size', size);
		return this.httpClient.get<PostResponse[] | HttpErrorResponse>(`${this.host}/users/${userId}/posts`, { params: reqParams });
	}

	verifyEmail(token: string): Observable<HttpResponse<any> | HttpErrorResponse> {
		return this.httpClient.post<HttpResponse<any> | HttpErrorResponse>(`${this.host}/verify-email/${token}`, null);
	}

	updateUserInfo(updateUserInfo: UpdateUserInfo): Observable<User | HttpErrorResponse> {
		return this.httpClient.post<User | HttpErrorResponse>(`${this.host}/account/update/info`, updateUserInfo);
	}

	updateUserEmail(updateUserEmail: UpdateUserEmail): Observable<any | HttpErrorResponse> {
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/account/update/email`, updateUserEmail);
	}

	updateUserPassword(updateUserPassword: UpdateUserPassword): Observable<any | HttpErrorResponse> {
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/account/update/password`, updateUserPassword);
	}

	updateProfilePhoto(profilePhoto: File): Observable<User | HttpErrorResponse> {
		const formData = new FormData();
		formData.append('profilePhoto', profilePhoto);
		return this.httpClient.post<User | HttpErrorResponse>(`${this.host}/account/update/profile-photo`, formData);
	}

	updateCoverPhoto(coverPhoto: File): Observable<User | HttpErrorResponse> {
		const formData = new FormData();
		formData.append('coverPhoto', coverPhoto);
		return this.httpClient.post<User | HttpErrorResponse>(`${this.host}/account/update/cover-photo`, formData);
	}

	followUser(userId: number): Observable<any | HttpErrorResponse> {
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/account/follow/${userId}`, null);
	}

	unfollowUser(userId: number): Observable<any | HttpErrorResponse> {
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/account/unfollow/${userId}`, null);
	}

	getUserSearchResult(key: string, page: number, size: number): Observable<UserResponse[] | HttpErrorResponse> {
		const reqParams = new HttpParams().set('key', key).set('page', page).set('size', size);
		return this.httpClient.get<UserResponse[] | HttpErrorResponse>(`${this.host}/users/search`, { params: reqParams });
	}

	forgotPassword(email: string): Observable<any | HttpErrorResponse> {
		const reqParams = new HttpParams().set('email', email);
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/forgot-password`, null, { params: reqParams });
	}

	resetPassword(token: string, resetPassword: ResetPassword): Observable<any | HttpErrorResponse> {
		return this.httpClient.post<any | HttpErrorResponse>(`${this.host}/reset-password/${token}`, resetPassword);
	}
}
