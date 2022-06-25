import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { RepeatPasswordMatcher } from 'src/app/common/repeat-password-matcher';
import { User } from 'src/app/model/user';
import { UserSignup } from 'src/app/model/user-signup';
import { AuthService } from 'src/app/service/auth.service';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-signup',
	templateUrl: './signup.component.html',
	styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit, OnDestroy {
	repeatPasswordMatcher = new RepeatPasswordMatcher();
	submittingForm: boolean = false;
	signupFormGroup: FormGroup;

	private subscriptions: Subscription[] = [];

	constructor(
		private authService: AuthService,
		private router: Router,
		private formBuilder: FormBuilder,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (this.authService.isUserLoggedIn()) {
			this.router.navigateByUrl('/profile');
		}

		this.signupFormGroup = this.formBuilder.group({
			infoGroup: this.formBuilder.group({
				firstName: new FormControl('',
					[Validators.required, Validators.maxLength(64)]
				),
				lastName: new FormControl('',
					[Validators.required, Validators.maxLength(64)]
				),
				email: new FormControl('',
					[Validators.required, Validators.email, Validators.maxLength(64)]
				)
			}),
			passwordGroup: this.formBuilder.group({
				password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(32)]),
				passwordRepeat: new FormControl('', [Validators.required])
			})
		}, { validators: this.matchPasswords });

	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	get firstName() { return this.signupFormGroup.get('infoGroup.firstName') }
	get lastName() { return this.signupFormGroup.get('infoGroup.lastName') }
	get email() { return this.signupFormGroup.get('infoGroup.email') }
	get password() { return this.signupFormGroup.get('passwordGroup.password') }
	get passwordRepeat() { return this.signupFormGroup.get('passwordGroup.passwordRepeat') }

	matchPasswords: ValidatorFn = (group: FormGroup): ValidationErrors | null => {
		const password = group.get('passwordGroup.password').value;
		const passwordRepeat = group.get('passwordGroup.passwordRepeat').value;
		return password === passwordRepeat ? null : { passwordMissMatch: true }
	}

	handleSignup(): void {
		if (this.signupFormGroup.valid) {
			this.submittingForm = true;
			const userSignup = new UserSignup();
			userSignup.firstName = this.firstName?.value;
			userSignup.lastName = this.lastName?.value;
			userSignup.email = this.email?.value;
			userSignup.password = this.password?.value;
			userSignup.passwordRepeat = this.passwordRepeat?.value;

			this.subscriptions.push(
				this.authService.signup(userSignup).subscribe({
					next: (response: HttpResponse<User>) => {
						localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.successLabel);
						localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.signupSuccessHeader);
						localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.signupSuccessDetail);
						localStorage.setItem(AppConstants.toLoginLabel, AppConstants.trueLabel);
						this.submittingForm = false;
						this.router.navigateByUrl('/message');
					},
					error: (errorResponse: HttpErrorResponse) => {
						const validationErrors = errorResponse.error.validationErrors;
						if (validationErrors != null) {
							Object.keys(validationErrors).forEach(key => {
								let formGroup = 'infoGroup';
								if (key === 'password' || key === 'passwordRepeat') formGroup = 'passwordGroup';
								const formControl = this.signupFormGroup.get(`${formGroup}.${key}`);
								if (formControl) {
									formControl.setErrors({
										serverError: validationErrors[key]
									});
								}
							})
						} else {
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: AppConstants.snackbarErrorContent,
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
}
