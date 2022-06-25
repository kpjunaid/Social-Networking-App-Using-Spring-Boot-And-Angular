import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { Post } from 'src/app/model/post';
import { PostResponse } from 'src/app/model/post-response';
import { PostService } from 'src/app/service/post.service';
import { environment } from 'src/environments/environment';
import { PostLikeDialogComponent } from '../post-like-dialog/post-like-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-post-share-dialog',
	templateUrl: './post-share-dialog.component.html',
	styleUrls: ['./post-share-dialog.component.css']
})
export class PostShareDialogComponent implements OnInit, OnDestroy {
	postShareResponseList: PostResponse[] = [];
	resultPage: number = 1;
	resultSize: number = 5;
	hasMoreResult: boolean = false;
	fetchingResult: boolean = false;
	defaultProfilePhotoUrl = environment.defaultProfilePhotoUrl;

	private subscriptions: Subscription[] = [];

	constructor(
		@Inject(MAT_DIALOG_DATA) public dataPost: Post,
		private postService: PostService,
		private matDialog: MatDialog,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (this.dataPost.shareCount > 0) {
			this.loadPostShares(1);
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	loadPostShares(currentPage: number): void {
		if (!this.fetchingResult) {
			this.fetchingResult = true;
			this.subscriptions.push(
				this.postService.getPostShares(this.dataPost.id, currentPage, this.resultSize).subscribe({
					next: (resultList: PostResponse[]) => {
						resultList.forEach(postShareResponse => this.postShareResponseList.push(postShareResponse));
						if (currentPage * this.resultSize < this.dataPost.shareCount) {
							this.hasMoreResult = true;
						} else {
							this.hasMoreResult = false;
						}
						this.resultPage++;
						this.fetchingResult = false;
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
	}

	likeOrUnlikePostShare(likedByAuthUser: boolean, postResponse: PostResponse) {
		if (likedByAuthUser) {
			this.subscriptions.push(
				this.postService.unlikePost(postResponse.post.id).subscribe({
					next: (response: any) => {
						postResponse.likedByAuthUser = false;
						postResponse.post.likeCount--;
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
		} else {
			this.subscriptions.push(
				this.postService.likePost(postResponse.post.id).subscribe({
					next: (response: any) => {
						postResponse.likedByAuthUser = true;
						postResponse.post.likeCount++;
					},
					error: (errorResponse: HttpErrorResponse) => {
						this.matSnackbar.openFromComponent(SnackbarComponent, {
							data: AppConstants.snackbarErrorContent,
							panelClass: ['bg-'],
							duration: 5000
						});
					}
				})
			);
		}
	}

	openPostLikeDialog(postResponse: PostResponse): void {
		this.matDialog.open(PostLikeDialogComponent, {
			data: postResponse.post,
			minWidth: '500px',
			maxWidth: '700px'
		});
	}
}
