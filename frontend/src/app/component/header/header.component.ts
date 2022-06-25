import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConstants } from 'src/app/common/app-constants';
import { Notification } from 'src/app/model/notification';
import { User } from 'src/app/model/user';
import { AuthService } from 'src/app/service/auth.service';
import { NotificationService } from 'src/app/service/notification.service';
import { environment } from 'src/environments/environment';
import { PostDialogComponent } from '../post-dialog/post-dialog.component';
import { SearchDialogComponent } from '../search-dialog/search-dialog.component';
import { SnackbarComponent } from '../snackbar/snackbar.component';

@Component({
	selector: 'app-header',
	templateUrl: './header.component.html',
	styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
	authUser: User;
	isUserLoggedIn: boolean = false;
	isProfilePage: boolean = false;
	notifications: Notification[] = [];
	hasUnseenNotification: boolean = false;
	resultPage: number = 1;
	resultSize: number = 5;
	hasMoreNotifications: boolean = false;
	fetchingResult: boolean = false;
	defaultProfilePhotoUrl = environment.defaultProfilePhotoUrl;
	
	private subscriptions: Subscription[] = [];

	constructor(
		private authService: AuthService,
		private notificationService: NotificationService,
		private matDialog: MatDialog,
		private matSnackbar: MatSnackBar) { }

	ngOnInit(): void {
		if (this.authService.isUserLoggedIn()) {
			this.isUserLoggedIn = true;
			this.authUser = this.authService.getAuthUserFromCache();
		} else {
			this.isUserLoggedIn = false;
		}

		if (this.isUserLoggedIn) {
			this.loadNotifications(1);
		}

		this.authService.logoutSubject.subscribe(loggedOut => {
			if (loggedOut) {
				this.isUserLoggedIn = false;
			}
		});

		this.authService.loginSubject.subscribe(loggedInUser => {
			if (loggedInUser) {
				this.authUser = loggedInUser;
				this.isUserLoggedIn = true;
			}
		});
	}

	ngOnDestroy(): void {
		this.subscriptions.forEach(sub => sub.unsubscribe());
	}

	openPostDialog(): void {
		this.matDialog.open(PostDialogComponent, {
			data: null,
			autoFocus: false,
			minWidth: '500px',
			maxWidth: '700px'
		});
	}

	openSearchDialog(): void {
		this.matDialog.open(SearchDialogComponent, {
			autoFocus: true,
			width: '500px'
		});
	}

	loadNotifications(page: number): void {
		this.fetchingResult = true;

		this.subscriptions.push(
			this.notificationService.getNotifications(page,  this.resultSize).subscribe({
				next: (notifications: Notification[]) => {
					this.fetchingResult = false;

					notifications.forEach(n => {
						this.notifications.push(n);
						if (!n.isSeen) this.hasUnseenNotification = true;
					});

					if (notifications.length > 0) {
						this.hasMoreNotifications = true;
					} else {
						this.hasMoreNotifications = false;
					}

					this.resultPage++;
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

	handleUnseenNotifications(): void {
		if (this.hasUnseenNotification) {
			this.subscriptions.push(
				this.notificationService.markAllSeen().subscribe({
					next: (response: any) => {
						this.hasUnseenNotification = false;
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
