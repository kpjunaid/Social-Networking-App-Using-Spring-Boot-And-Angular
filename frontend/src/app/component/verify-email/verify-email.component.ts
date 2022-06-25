import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { UserService } from 'src/app/service/user.service';

@Component({
	selector: 'app-verify-email',
	templateUrl: './verify-email.component.html',
	styleUrls: ['./verify-email.component.css']
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
	token: string;
	private jwtService = new JwtHelperService();
	private subscriptions: Subscription[] = [];

	constructor(
		private userService: UserService,
		private router: Router,
		private route: ActivatedRoute) { }

	ngOnInit(): void {
		this.token = this.route.snapshot.paramMap.get('token');

		if (this.token !== null) {
			if (!this.jwtService.isTokenExpired(this.token)) {
				this.subscriptions.push(
					this.userService.verifyEmail(this.token).subscribe(
						(response: HttpResponse<any>) => {
							localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.successLabel);
							localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.emailVerifySuccessHeader);
							localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.emailVerifySuccessDetail);
							this.router.navigateByUrl('/message');
						},
						(errorResponse: HttpErrorResponse) => {
							localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.errorLabel);
							localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.tokenErrorHeader);
							localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.tokenErrorDetail);
							this.router.navigateByUrl('/message');
						}
					)
				);
			} else {
				localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.errorLabel);
				localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.tokenErrorHeader);
				localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.tokenErrorDetail);
				this.router.navigateByUrl('/message');
			}
		} else {
			this.router.navigateByUrl('/');
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}
}
