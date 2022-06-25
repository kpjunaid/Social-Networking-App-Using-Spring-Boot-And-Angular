import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { User } from 'src/app/model/user';
import { AuthService } from 'src/app/service/auth.service';
import { PostService } from 'src/app/service/post.service';
import { UserService } from 'src/app/service/user.service';
import { environment } from 'src/environments/environment';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-photo-upload-dialog',
	templateUrl: './photo-upload-dialog.component.html',
	styleUrls: ['./photo-upload-dialog.component.css']
})
export class PhotoUploadDialogComponent implements OnInit {
	photoPreviewUrl: string;
	photo: File;
	defaultProfilePhotoUrl: string = environment.defaultProfilePhotoUrl;
	defaultCoverPhotoUrl: string = environment.defaultCoverPhotoUrl;

	private subscriptions: Subscription[] = [];

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: any,
		private authService: AuthService,
		private userService: UserService,
		private thisDialogRef: MatDialogRef<PhotoUploadDialogComponent>,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (this.data.uploadType === 'profilePhoto') {
			this.photoPreviewUrl = this.data.authUser.profilePhoto ? this.data.authUser.profilePhoto : this.defaultProfilePhotoUrl;
		} else if (this.data.uploadType === 'coverPhoto') {
			this.photoPreviewUrl = this.data.authUser.coverPhoto ? this.data.authUser.coverPhoto : this.defaultCoverPhotoUrl;
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	previewPhoto(e: any): void {
		if (e.target.files) {
			this.photo = e.target.files[0];
			const reader = new FileReader();
			reader.readAsDataURL(this.photo);
			reader.onload = (e: any) => {
				this.photoPreviewUrl = e.target.result;
			}
		}
	}

	savePhoto(): void {
		if (this.photo) {
			if (this.data.uploadType === 'profilePhoto') {
				this.subscriptions.push(
					this.userService.updateProfilePhoto(this.photo).subscribe({
						next: (updatedUser: User) => {
							this.authService.storeAuthUserInCache(updatedUser);
							this.photoPreviewUrl = null;
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: 'Profile photo updated successfully.',
								duration: 5000
							});
							this.thisDialogRef.close({ updatedUser });
						},
						error: (errorResponse: HttpErrorResponse) => {
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: AppConstants.snackbarErrorContent,
								panelClass: ['bg-danger'],
								duration: 5000
							});
						}
					})
				);
			} else if (this.data.uploadType === 'coverPhoto') {
				this.subscriptions.push(
					this.userService.updateCoverPhoto(this.photo).subscribe({
						next: (updatedUser: User) => {
							this.authService.storeAuthUserInCache(updatedUser);
							this.photoPreviewUrl = null;
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: 'Cover photo updated successfully.',
								duration: 5000
							});
							this.thisDialogRef.close({ updatedUser });
						},
						error: (errorResponse: HttpErrorResponse) => {
							this.matSnackbar.openFromComponent(SnackbarComponent, {
								data: AppConstants.snackbarErrorContent,
								panelClass: ['bg-danger'],
								duration: 5000
							});
						}
					})
				);
			}
		} else {
			this.matSnackbar.openFromComponent(SnackbarComponent, {
				data: 'Please, first upload a photo to save.',
				panelClass: ['bg-danger'],
				duration: 5000
			});
		}
	};
}