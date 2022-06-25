import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { Post } from 'src/app/model/post';
import { PostService } from 'src/app/service/post.service';
import { environment } from 'src/environments/environment';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';
import { TagDialogComponent } from '../tag-dialog/tag-dialog.component';

@Component({
	selector: 'app-post-dialog',
	templateUrl: './post-dialog.component.html',
	styleUrls: ['./post-dialog.component.css']
})
export class PostDialogComponent implements OnInit, OnDestroy {
	postFormGroup: FormGroup;
	postPhoto: File;
	postPhotoPreviewUrl: string;
	postTags: any[] = [];
	creatingPost: boolean = false;

	private subscriptions: Subscription[] = [];

	constructor(
		@Inject(MAT_DIALOG_DATA) public dataPost: Post,
		private postService: PostService,
		private formBuilder: FormBuilder,
		private router: Router,
		private matDialog: MatDialog,
		private matDialogRef: MatDialogRef<PostDialogComponent>,
		private matSnackbar: MatSnackBar) { }

	get content() { return this.postFormGroup.get('content'); }

	ngOnInit(): void {
		this.postFormGroup = this.formBuilder.group({
			content: new FormControl(((this.dataPost && this.dataPost.content) ? this.dataPost.content : ''), [Validators.maxLength(4096)])
		});

		if (this.dataPost) {
			if (this.dataPost.postPhoto) {
				this.postPhotoPreviewUrl = this.dataPost.postPhoto;
			}

			this.populateWithPostTags();
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	previewPostPhoto(event: any): void {
		if (event.target.files) {
			this.postPhoto = event.target.files[0];
			const reader = new FileReader();
			reader.readAsDataURL(this.postPhoto);
			reader.onload = (e: any) => {
				this.postPhotoPreviewUrl = e.target.result;
			}
		}
	}

	openPostPhotoDeleteConfirmDialog(): void {
		const dialogRef = this.matDialog.open(ConfirmationDialogComponent, {
			data: 'Do you want to delete this photo?',
			width: '500px',
			autoFocus: false
		});

		dialogRef.afterClosed().subscribe(
			result => {
				if (result) {
					this.deletePostPhoto();
				}
			}
		);
	}

	openAddTagDialog(e: Event): void {
		e.preventDefault();

		const dialogRef = this.matDialog.open(TagDialogComponent, {
			width: '500px',
			autoFocus: true
		});

		dialogRef.afterClosed().subscribe(
			result => {
				if (result) {
					const tagIndex = this.postTags.findIndex(tN => tN === result.tagName);
					if (tagIndex >= 0) {
						this.postTags[tagIndex].action = 'add'
					} else {
						this.postTags.push({
							tagName: result.tagName,
							action: 'add'
						})
					}
				}
				console.log(this.postTags)
			}
		);
	}

	removeTag(tagName: string): void {
		const tagIndex = this.postTags.findIndex(tN => tN === tagName);
		if (this.postTags[tagIndex].action === 'saved') {
			this.postTags[tagIndex].action = 'remove';
		} else {
			this.postTags.splice(tagIndex, 1);
		}
		console.log(this.postTags)
	}

	handlePostSubmit(): void {
		if (this.content.value.length <= 0 && !this.postPhoto) {
			this.matSnackbar.openFromComponent(SnackbarComponent, {
				data: 'Post cannot be empty.',
				panelClass: ['bg-danger'],
				duration: 5000
			});
			return;
		}

		if (this.dataPost) {
			this.updatePost();
		} else {
			this.createNewPost();
		}
	}

	private createNewPost(): void {
		if (!this.creatingPost) {
			this.creatingPost = true;
			this.subscriptions.push(
				this.postService.createNewPost(this.content.value, this.postPhoto, this.postTags).subscribe({
					next: (createdPost: Post) => {
						this.matDialogRef.close();
						this.matSnackbar.openFromComponent(SnackbarComponent, {
							data: 'Post created successfully.',
							duration: 5000
						});
						this.creatingPost = false;
						this.router.navigateByUrl(`/posts/${createdPost.id}`).then(() => {
							window.location.reload();
						});
					},
					error: (errorResponse: HttpErrorResponse) => {
						this.matSnackbar.openFromComponent(SnackbarComponent, {
							data: AppConstants.snackbarErrorContent,
							panelClass: ['bg-danger'],
							duration: 5000
						});
						this.creatingPost = false;
					}
				})
			);
		}
	}

	private updatePost(): void {
		this.subscriptions.push(
			this.postService.updatePost(this.dataPost.id, this.content.value, this.postPhoto, this.postTags).subscribe({
				next: (createdPost: Post) => {
					this.matDialogRef.close();
					this.matSnackbar.openFromComponent(SnackbarComponent, {
						data: 'Post updated successfully.',
						duration: 5000
					});
					this.router.navigateByUrl(`/posts/${createdPost.id}`);
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

	private deletePostPhoto(): void {
		this.subscriptions.push(
			this.postService.deletePostPhoto(this.dataPost.id).subscribe({
				next: (createdPost: Post) => {
					this.postPhotoPreviewUrl = null;
					this.matSnackbar.openFromComponent(SnackbarComponent, {
						data: 'Photo deleted successfully.',
						duration: 5000
					});
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

	private populateWithPostTags(): void {
		this.dataPost.postTags.forEach(tag => {
			this.postTags.push({
				tagName: tag.name,
				action: 'saved'
			});
		});
	}
}
