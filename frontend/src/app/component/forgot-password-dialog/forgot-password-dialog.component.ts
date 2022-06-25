import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { UserService } from 'src/app/service/user.service';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-forgot-password-dialog',
	templateUrl: './forgot-password-dialog.component.html',
	styleUrls: ['./forgot-password-dialog.component.css']
})
export class ForgotPasswordDialogComponent implements OnInit, OnDestroy {
	forgotPasswordFormGroup: FormGroup;
	fetchingResult: boolean = false;

	private subscriptions: Subscription[] = [];

	constructor(
		private userService: UserService,
		private formBuilder: FormBuilder,
		private matSnackbar: MatSnackBar,
		private thisDialogRef: MatDialogRef<ForgotPasswordDialogComponent>,
		private router: Router) { }

	get email() { return this.forgotPasswordFormGroup.get('email'); }

	ngOnInit(): void {
		this.forgotPasswordFormGroup = this.formBuilder.group({
			email: new FormControl('', [Validators.required, Validators.email, Validators.maxLength(64)])
		});
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	sendForgotPasswordEmail(): void {
		if (this.forgotPasswordFormGroup.valid) {
			if (!this.fetchingResult) {
				this.fetchingResult = true;
				this.subscriptions.push(
					this.userService.forgotPassword(this.email.value).subscribe({
						next: (result: any) => {
							localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.successLabel);
							localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.forgotPasswordSuccessHeader);
							localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.forgotPasswordSuccessDetail);
							localStorage.setItem(AppConstants.toLoginLabel, AppConstants.falseLabel);
							this.fetchingResult = false;
							this.thisDialogRef.close();
							this.router.navigateByUrl('/message');
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
