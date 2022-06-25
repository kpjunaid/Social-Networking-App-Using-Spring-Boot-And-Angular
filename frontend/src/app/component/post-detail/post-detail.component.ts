import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { Post } from 'src/app/model/post';
import { PostResponse } from 'src/app/model/post-response';
import { AuthService } from 'src/app/service/auth.service';
import { PostService } from 'src/app/service/post.service';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-post-detail',
	templateUrl: './post-detail.component.html',
	styleUrls: ['./post-detail.component.css']
})
export class PostDetailComponent implements OnInit, OnDestroy {
	postId: number;
	postResponse: PostResponse;
	fetchingResult: boolean = false;

	private subscriptions: Subscription[] = [];

	constructor(
		private authService: AuthService,
		private router: Router,
		private postService: PostService,
		private activatedRoute: ActivatedRoute,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (!this.authService.isUserLoggedIn()) {
			this.router.navigateByUrl('/login');
		} else {
			this.fetchingResult = true;
			this.postId = Number(this.activatedRoute.snapshot.paramMap.get('postId'));

			this.subscriptions.push(
				this.postService.getPostById(this.postId).subscribe({
					next: (postResponse: PostResponse) => {
						this.postResponse = postResponse;
						this.fetchingResult = false;
					},
					error: (errorResponse: HttpErrorResponse) => {
						localStorage.setItem(AppConstants.messageTypeLabel, AppConstants.errorLabel);
						localStorage.setItem(AppConstants.messageHeaderLabel, AppConstants.notFoundErrorHeader);
						localStorage.setItem(AppConstants.messageDetailLabel, AppConstants.notFoundErrorDetail);
						localStorage.setItem(AppConstants.toLoginLabel, AppConstants.falseLabel);
						this.fetchingResult = false;
						this.router.navigateByUrl('/message');
					}
				})
			);
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

}
