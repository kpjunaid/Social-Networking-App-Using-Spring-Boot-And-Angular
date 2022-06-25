import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { noop, Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { PostResponse } from 'src/app/model/post-response';
import { Tag } from 'src/app/model/tag';
import { AuthService } from 'src/app/service/auth.service';
import { PostService } from 'src/app/service/post.service';
import { TimelineService } from 'src/app/service/timeline.service';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-timeline',
	templateUrl: './timeline.component.html',
	styleUrls: ['./timeline.component.css']
})
export class TimelineComponent implements OnInit, OnDestroy {
	timelinePostResponseList: PostResponse[] = [];
	timelineTagList: Tag[] = [];
	noPost: boolean = false;
	resultPage: number = 1;
	resultSize: number = 5;
	hasMoreResult: boolean = true;
	fetchingResult: boolean = false;
	isTaggedPostPage: boolean = false;
	targetTagName: string;
	loadingTimelinePostsInitially: boolean = true;
	loadingTimelineTagsInitially: boolean = true;

	private subscriptions: Subscription[] = [];

	constructor(
		private authService: AuthService,
		private timelineService: TimelineService,
		private postService: PostService,
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (!this.authService.isUserLoggedIn()) {
			this.router.navigateByUrl('/login');
		} else {
			if (this.router.url !== '/') {
				this.targetTagName = this.activatedRoute.snapshot.paramMap.get('tagName');
				this.isTaggedPostPage = true;
				this.loadTaggedPosts(this.targetTagName, 1);
			} else {
				this.loadTimelinePosts(1);
			}

			this.loadTimelineTags();
		}
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	loadTimelinePosts(currentPage: number): void {
		if (!this.fetchingResult) {
			this.fetchingResult = true;
			this.subscriptions.push(
				this.timelineService.getTimelinePosts(currentPage, this.resultSize).subscribe({
					next: (postResponseList: PostResponse[]) => {
						if (postResponseList.length === 0 && currentPage === 1) this.noPost = true;
						
						postResponseList.forEach(pR => this.timelinePostResponseList.push(pR));

						if (postResponseList.length > 0) {
							this.hasMoreResult = true;
						} else {
							this.hasMoreResult = false;
						}

						this.resultPage++;
						this.fetchingResult = false;
						this.loadingTimelinePostsInitially = false;
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

	loadTaggedPosts(tagName: string, currentPage: number): void {
		if (!this.fetchingResult) {
			this.fetchingResult = true;
			this.subscriptions.push(
				this.postService.getPostsByTag(tagName, currentPage, this.resultSize).subscribe({
					next: (postResponseList: PostResponse[]) => {
						if (postResponseList.length === 0 && currentPage === 1) this.noPost = true;
						
						postResponseList.forEach(pR => this.timelinePostResponseList.push(pR));
						if (postResponseList.length > 0) {
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

	loadTimelineTags(): void {
		this.fetchingResult = true;
		this.subscriptions.push(
			this.timelineService.getTimelineTags().subscribe({
				next: (tagList: Tag[]) => {
					tagList.forEach(t => this.timelineTagList.push(t));
					this.loadingTimelineTagsInitially = false;
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
