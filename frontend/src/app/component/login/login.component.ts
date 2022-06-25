import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { User } from 'src/app/model/user';
import { UserLogin } from 'src/app/model/user-login';
import { AuthService } from 'src/app/service/auth.service';
import { ForgotPasswordDialogComponent } from '../forgot-password-dialog/forgot-password-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
	loginFormGroup: FormGroup;
	submittingForm: boolean = false;

	private subscriptions: Subscription[] = [];

	constructor(
		private authService: AuthService,
		private router: Router,
		private formBuilder: FormBuilder,
		private matSnackbar: MatSnackBar,
		private matDialog: MatDialog) { }

	get email() { return this.loginFormGroup.get('email') }
	get password() { return this.loginFormGroup.get('password') }

	ngOnInit(): void {
		if (this.authService.isUserLoggedIn()) {
			this.router.navigateByUrl('/profile');
			return;
		}

		this.loginFormGroup = this.formBuilder.group({
			email: new FormControl('',
				[Validators.required, Validators.email]
			),
			password: new FormControl('',
				[Validators.required, Validators.minLength(6), Validators.maxLength(32)]
			)
		});
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	handleLogin(): void {
		if (this.loginFormGroup.valid) {
			this.submittingForm = true;
			const userLogin = new UserLogin();
			userLogin.email = this.email?.value;
			userLogin.password = this.password?.value;
			this.subscriptions.push(
				this.authService.login(userLogin).subscribe({
					next: (response: HttpResponse<User>) => {
						const authToken = response.headers.get('Jwt-Token');
						this.authService.storeTokenInCache(authToken);
						this.authService.storeAuthUserInCache(response.body);
						this.submittingForm = false;
						this.router.navigateByUrl('/profile');
					},
					error: (errorResponse: HttpErrorResponse) => {
						const validationErrors = errorResponse.error.validationErrors;
						if (validationErrors != null) {
							Object.keys(validationErrors).forEach(key => {
								const formControl = this.loginFormGroup.get(key);
								if (formControl) {
									formControl.setErrors({
										serverError: validationErrors[key]
									});
								}
							})
						} else {
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: 'Incorrect email or password.',
								panelClass: ['bg-danger'],
								duration: 5000
							});
						}
						this.submittingForm = false;
					}
				})
			);
		}
	}

	openForgotPasswordDialog(e: Event): void {
		e.preventDefault();
		this.matDialog.open(ForgotPasswordDialogComponent, {
			autoFocus: true,
			width: '500px'
		});
	}
}
