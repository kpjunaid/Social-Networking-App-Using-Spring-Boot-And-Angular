import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, Output, OnDestroy, OnInit, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { PostResponse } from 'src/app/model/post-response';
import { AuthService } from 'src/app/service/auth.service';
import { PostService } from 'src/app/service/post.service';
import { environment } from 'src/environments/environment';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { PostCommentDialogComponent } from '../post-comment-dialog/post-comment-dialog.component';
import { PostDialogComponent } from '../post-dialog/post-dialog.component';
import { PostLikeDialogComponent } from '../post-like-dialog/post-like-dialog.component';
import { PostShareDialogComponent } from '../post-share-dialog/post-share-dialog.component';
import { ShareConfirmDialogComponent } from '../share-confirm-dialog/share-confirm-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';
import { WaitingDialogComponent } from '../waiting-dialog/waiting-dialog.component';

@Component({
	selector: 'app-post',
	templateUrl: './post.component.html',
	styleUrls: ['./post.component.css']
})
export class PostComponent implements OnInit, OnDestroy {
	@Input() postResponse: PostResponse;
	@Input() isDetailedPost: boolean;
	@Output() postDeletedEvent = new EventEmitter<PostResponse>();
	authUserId: number;
	defaultProfilePhotoUrl = environment.defaultProfilePhotoUrl;

	private subscriptions: Subscription[] = [];

	constructor(
		private matDialog: MatDialog,
		private matSnackbar: MatSnackBar,
		private authService: AuthService,
		private postService: PostService) { }

	ngOnInit(): void {
		this.authUserId = this.authService.getAuthUserId();
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	openLikeDialog(): void {
		this.matDialog.open(PostLikeDialogComponent, {
			data: this.postResponse.post,
			minWidth: '400px',
			maxWidth: '700px'
		});
	}

	openCommentDialog(): void {
		const dialogRef = this.matDialog.open(PostCommentDialogComponent, {
			data: this.postResponse.post,
			autoFocus: false,
			minWidth: '500px',
			maxWidth: '700px'
		});

		dialogRef.componentInstance.updatedCommentCountEvent.subscribe(
			data => this.postResponse.post.commentCount = data
		);
	}

	openShareDialog(): void {
		const dialogRef = this.matDialog.open(PostShareDialogComponent, {
			data: this.postResponse.post,
			autoFocus: false,
			minWidth: '500px',
			maxWidth: '700px'
		});
	}

	openShareConfirmDialog(): void {
		this.matDialog.open(ShareConfirmDialogComponent, {
			data: this.postResponse.post,
			autoFocus: false,
			minWidth: '500px',
			maxWidth: '700px'
		});
	}

	openPostEditDialog(): void {
		const dialogRef = this.matDialog.open(PostDialogComponent, {
			data: this.postResponse.post,
			autoFocus: false,
			minWidth: '500px',
			maxWidth: '900px'
		});
	}

	openPostDeleteConfirmDialog(): void {
		const dialogRef = this.matDialog.open(ConfirmationDialogComponent, {
			data: 'Do you want to delete this post permanently?',
			autoFocus: false,
			width: '500px'
		});

		dialogRef.afterClosed().subscribe(
			result => {
				if (result) this.deletePost(this.postResponse.post.id, this.postResponse.post.isTypeShare);
			}
		);
	}

	deletePost(postId: number, isTypeShare: boolean): void {
		const dialogRef = this.matDialog.open(WaitingDialogComponent, {
			data: 'Please, wait while we are deleting the post.',
			width: '500px',
			disableClose: true
		});

		this.subscriptions.push(
			this.postService.deletePost(postId, isTypeShare).subscribe({
				next: (response: any) => {
					this.postDeletedEvent.emit(this.postResponse);
					dialogRef.close();
					this.matSnackbar.openFromComponent(SnackbarComponent, {
						data: 'Post deleted successfully.',
						panelClass: ['bg-success'],
						duration: 5000
					});
				},
				error: (errorResponse: HttpErrorResponse) => {
					this.matSnackbar.openFromComponent(SnackbarComponent, {
						data: AppConstants.snackbarErrorContent,
						panelClass: ['bg-danger'],
						duration: 5000
					});
					dialogRef.close();
				}
			})
		);
	}

	likeOrUnlikePost(likedByAuthUser: boolean) {
		if (likedByAuthUser) {
			this.subscriptions.push(
				this.postService.unlikePost(this.postResponse.post.id).subscribe({
					next: (response: any) => {
						this.postResponse.likedByAuthUser = false;
						this.postResponse.post.likeCount--;
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
				this.postService.likePost(this.postResponse.post.id).subscribe({
					next: (response: any) => {
						this.postResponse.likedByAuthUser = true;
						this.postResponse.post.likeCount++;
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
}
