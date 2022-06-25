import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment';
import { User } from '../model/user';
import { UserLogin } from '../model/user-login';
import { UserSignup } from '../model/user-signup';

@Injectable({
	providedIn: 'root'
})
export class AuthService {
	logoutSubject = new Subject<boolean>();
	loginSubject = new Subject<User>();
	private host = environment.apiUrl;
	private authToken: string;
	private authUser: User;
	private principal: string;
	private jwtService = new JwtHelperService();

	constructor(private httpClient: HttpClient) { }

	signup(userSignup: UserSignup): Observable<HttpResponse<any> | HttpErrorResponse> {
		return this.httpClient.post<HttpResponse<any> | HttpErrorResponse>(`${this.host}/signup`, userSignup);
	}

	login(userLogin: UserLogin): Observable<HttpResponse<User> | HttpErrorResponse> {
		return this.httpClient.post<User>(`${this.host}/login`, userLogin, { observe: 'response' });
	}

	logout(): void {
		this.authToken = null;
		this.authUser = null;
		this.principal = null;
		localStorage.removeItem('authUser');
		localStorage.removeItem('authToken');
		this.logoutSubject.next(true);
	}

	storeTokenInCache(authToken: string): void {
		if (authToken != null && authToken != '') {
			this.authToken = authToken;
			localStorage.setItem('authToken', authToken);
		}
	}

	loadAuthTokenFromCache(): void {
		this.authToken = localStorage.getItem('authToken');
	}

	getAuthTokenFromCache(): string {
		return localStorage.getItem('authToken');
	}

	storeAuthUserInCache(authUser: User): void {
		if (authUser != null) {
			this.authUser = authUser;
			localStorage.setItem('authUser', JSON.stringify(authUser));
		}
		this.loginSubject.next(authUser);
	}

	getAuthUserFromCache(): User {
		return JSON.parse(localStorage.getItem('authUser'));
	}

	getAuthUserId(): number {
		return this.getAuthUserFromCache().id;
	}

	isUserLoggedIn(): boolean {
		this.loadAuthTokenFromCache();

		if (this.authToken != null && this.authToken != '') {
			if (this.jwtService.decodeToken(this.authToken).sub != null || '') {
				if (!this.jwtService.isTokenExpired(this.authToken)) {
					this.principal = this.jwtService.decodeToken(this.authToken).sub;
					return true;
				}
			}
		}

		this.logout();
		return false;
	}
}
