import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { Post } from 'src/app/model/post';
import { User } from 'src/app/model/user';
import { PostService } from 'src/app/service/post.service';
import { environment } from 'src/environments/environment';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-post-like-dialog',
	templateUrl: './post-like-dialog.component.html',
	styleUrls: ['./post-like-dialog.component.css']
})
export class PostLikeDialogComponent implements OnInit, OnDestroy {
	likeList: User[] = [];
	resultPage: number = 1;
	resultSize: number = 5;
	hasMoreResult: boolean = false;
	fetchingResult: boolean = false;
	defaultProfilePhotoUrl = environment.defaultProfilePhotoUrl;

	private subscriptions: Subscription[] = [];

	constructor(
		@Inject(MAT_DIALOG_DATA) public dataPost: Post,
		private postService: PostService,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		this.loadLikes(1);
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	loadLikes(currentPage: number): void {
		if (!this.fetchingResult) {
			if (this.dataPost.likeCount > 0) {
				this.fetchingResult = true;
				this.subscriptions.push(
					this.postService.getPostLikes(this.dataPost.id, currentPage, this.resultSize).subscribe({
						next: (resultList: User[]) => {
							resultList.forEach(like => this.likeList.push(like));
							if (currentPage * this.resultSize < this.dataPost.likeCount) {
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
							this.fetchingResult = false;
						}
					})
				);
			}
		}
	}
}
