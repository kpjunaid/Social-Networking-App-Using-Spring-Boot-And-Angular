import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthService } from '../service/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
	private host = environment.apiUrl;

	constructor(private authService: AuthService) { }

	intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		if (request.url.includes(`${this.host}/signup`)) {
			return next.handle(request);
		}

		if (request.url.includes(`${this.host}/login`)) {
			return next.handle(request);
		}

		if (request.url.includes(`${this.host}/verify-email`)) {
			return next.handle(request);
		}

		if (request.url.includes(`${this.host}/forgot-password`)) {
			return next.handle(request);
		}

		if (request.url.includes(`${this.host}/reset-password`)) {
			return next.handle(request);
		}

		const authToken = this.authService.getAuthTokenFromCache();
		const newRequest = request.clone({setHeaders: {Authorization: `Bearer ${authToken}`}});
		return next.handle(newRequest);
	}
}
