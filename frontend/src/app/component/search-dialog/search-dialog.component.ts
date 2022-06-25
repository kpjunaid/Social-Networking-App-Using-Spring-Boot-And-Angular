import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { UserResponse } from 'src/app/model/user-response';
import { UserService } from 'src/app/service/user.service';
import { environment } from 'src/environments/environment';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-search-dialog',
	templateUrl: './search-dialog.component.html',
	styleUrls: ['./search-dialog.component.css']
})
export class SearchDialogComponent implements OnInit, OnDestroy {
	searchResult: UserResponse[] = [];
	searchUserFormGroup: FormGroup;
	resultPage: number = 1;
	resultSize: number = 5;
	hasMoreResult: boolean = false;
	noResult: boolean = false;
	fetchingResult: boolean = false;
	defaultProfilePhotoUrl: string = environment.defaultProfilePhotoUrl;

	private subscriptions: Subscription[] = [];

	constructor(
		private userService: UserService,
		private formBuilder: FormBuilder,
		private matSnackbar: MatSnackBar,
		private matDialog: MatDialog,
		private router: Router) { }

	get key() { return this.searchUserFormGroup.get('key'); }

	ngOnInit(): void {
		this.searchUserFormGroup = this.formBuilder.group({
			key: new FormControl('', [Validators.minLength(3), Validators.maxLength(64)])
		});
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	searchUser(currentPage: number): void {
		if (!this.fetchingResult) {
			if (this.key.value.length >= 3) {
				this.fetchingResult = true;
	
				if (currentPage === 1) this.searchResult = [];
	
				this.subscriptions.push(
					this.userService.getUserSearchResult(this.key.value, currentPage, this.resultSize).subscribe({
						next: (resultList: UserResponse[]) => {
							if (resultList.length <= 0 && currentPage === 1) {
								this.noResult = true;
							} else {
								this.noResult = false;
							}
	
							resultList.forEach(uR => this.searchResult.push(uR));
							this.resultPage++;
							this.fetchingResult = false;
	
							if (resultList.length < this.resultSize) {
								this.hasMoreResult = false;
								this.resultPage = 1;
							} else {
								this.hasMoreResult = true;
							}
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
			} else {
				this.matSnackbar.openFromComponent(SnackbarComponent, {
					data: 'Search key must be between 3 to 64 characters long.',
					panelClass: ['bg-danger'],
					duration: 5000
				});
			}
		}
	} 

	openFollowConfirmDialog(userResponse: UserResponse): void {
		const dialogRef = this.matDialog.open(ConfirmationDialogComponent, {
			data: `Do you want to follow this ${userResponse.user.firstName + ' ' + userResponse.user.lastName}?`,
			autoFocus: false,
			maxWidth: '500px'
		});

		dialogRef.afterClosed().subscribe(
			(result) => {
				if (result) {
					this.subscriptions.push(
						this.userService.followUser(userResponse.user.id).subscribe({
							next: (response: any) => {
								const targetResult = this.searchResult.find(uR => uR === userResponse);
								targetResult.followedByAuthUser = true;

								this.matSnackbar.openFromComponent(SnackbarComponent, {
									data: `You are now following ${userResponse.user.firstName + ' ' + userResponse.user.lastName}.`,
									duration: 5000
								});
							},
							error: (errorResponse: HttpErrorResponse) => {
								this.matSnackbar.openFromComponent(SnackbarComponent, {
									data: AppConstants.snackbarErrorContent,
									panelClass: 'bg-danger',
									duration: 5000
								});
							}
						})
					);
				}
			}
		);
	}

	openUnfollowConfirmDialog(userResponse: UserResponse): void {
		const dialogRef = this.matDialog.open(ConfirmationDialogComponent, {
			data: `Do you want to stop following ${userResponse.user.firstName + ' ' + userResponse.user.lastName}?`,
			autoFocus: false,
			maxWidth: '500px'
		});

		dialogRef.afterClosed().subscribe(
			(result) => {
				if (result) {
					this.subscriptions.push(
						this.userService.unfollowUser(userResponse.user.id).subscribe({
							next: (response: any) => {
								const targetResult = this.searchResult.find(uR => uR === userResponse);
								targetResult.followedByAuthUser = false;

								this.matSnackbar.openFromComponent(SnackbarComponent, {
									data: `You no longer follow ${userResponse.user.firstName + ' ' + userResponse.user.lastName}.`,
									duration: 5000
								});
							},
							error: (errorResponse: HttpErrorResponse) => {
								this.matSnackbar.openFromComponent(SnackbarComponent, {
									data: AppConstants.snackbarErrorContent,
									panelClass: 'bg-danger',
									duration: 5000
								});
							}
						})
					);
				}
			}
		);
	}
}
