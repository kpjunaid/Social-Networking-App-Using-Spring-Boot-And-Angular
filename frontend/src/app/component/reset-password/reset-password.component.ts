import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { RepeatPasswordMatcher } from 'src/app/common/repeat-password-matcher';
import { ResetPassword } from 'src/app/model/reset-password';
import { AuthService } from 'src/app/service/auth.service';
import { UserService } from 'src/app/service/user.service';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-reset-password',
	templateUrl: './reset-password.component.html',
	styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
	token: string;
	resetPasswordFormGroup: FormGroup;
	fetchingResult: boolean = false;
	repeatPasswordMatcher = new RepeatPasswordMatcher();

	private subscriptions: Subscription[] = [];

	constructor(
		private userService: UserService,
		private router: Router,
		private formBuilder: FormBuilder,
		private matSnackbar: MatSnackBar,
		private activatedRoute: ActivatedRoute,
		private matDialog: MatDialog) { }

	get password() { return this.resetPasswordFormGroup.get('password') }
	get passwordRepeat() { return this.resetPasswordFormGroup.get('passwordRepeat') }

	ngOnInit(): void {
		this.token = this.activatedRoute.snapshot.paramMap.get('token');

		this.resetPasswordFormGroup = this.formBuilder.group({
			password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(32)]),
			passwordRepeat: new FormControl('', [Validators.required])
		}, { validators: this.matchPasswords });
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	matchPasswords: ValidatorFn = (group: FormGroup): ValidationErrors | null => {
		const password = group.get('password').value;
		const passwordRepeat = group.get('passwordRepeat').value;
		return password === passwordRepeat ? null : { passwordMissMatch: true }
	}

	resetPassword(): void {
		if (this.resetPasswordFormGroup.valid) {
			if (!this.fetchingResult) {
				this.fetchingResult = true;
				const resetPassword = new ResetPassword();
				resetPassword.password = this.password?.value;
				resetPassword.passwordRepeat = this.passwordRepeat?.value;

				this.subscriptions.push(
					this.userService.resetPassword(this.token, resetPassword).subscribe({
						next: (result: any) => {
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: 'Your password has been changed successfully.',
								duration: 5000
							});
							this.fetchingResult = false;
							this.router.navigateByUrl('/login');
						},
						error: (errorResponse: HttpErrorResponse) => {
							this.fetchingResult = false;
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: AppConstants.snackbarErrorContent,
								panelClass: ['bg-danger'],
								duration: 5000
							});
						}
					})
				);
			}
		}
	}
}
