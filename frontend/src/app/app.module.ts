import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRippleModule, MAT_DATE_FORMATS } from '@angular/material/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatMomentDateModule } from "@angular/material-moment-adapter";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthInterceptor } from './interceptor/auth.interceptor';
import { AuthGuard } from './guard/auth.guard';
import { LoginComponent } from './component/login/login.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HeaderComponent } from './component/header/header.component';
import { SignupComponent } from './component/signup/signup.component';
import { ProfileComponent } from './component/profile/profile.component';
import { TimelineComponent } from './component/timeline/timeline.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LogoutComponent } from './component/logout/logout.component';
import { VerifyEmailComponent } from './component/verify-email/verify-email.component';
import { MessageComponent } from './component/message/message.component';
import { ErrorPageComponent } from './component/error-page/error-page.component';
import { PostComponent } from './component/post/post.component';
import { PostLikeDialogComponent } from './component/post-like-dialog/post-like-dialog.component';
import { PostShareDialogComponent } from './component/post-share-dialog/post-share-dialog.component';
import { PostCommentDialogComponent } from './component/post-comment-dialog/post-comment-dialog.component';
import { CommentLikeDialogComponent } from './component/comment-like-dialog/comment-like-dialog.component';
import { ConfirmationDialogComponent } from './component/confirmation-dialog/confirmation-dialog.component';
import { ShareConfirmDialogComponent } from './component/share-confirm-dialog/share-confirm-dialog.component';
import { SnackbarComponent } from './component/snackbar/snackbar.component';
import { FollowingFollowerListDialogComponent } from './component/following-follower-list-dialog/following-follower-list-dialog.component';
import { ViewPhotoDialogComponent } from './component/view-photo-dialog/view-photo-dialog.component';
import { PostDialogComponent } from './component/post-dialog/post-dialog.component';
import { PostDetailComponent } from './component/post-detail/post-detail.component';
import { WaitingDialogComponent } from './component/waiting-dialog/waiting-dialog.component';
import { SettingsComponent } from './component/settings/settings.component';
import { APP_DATE_FORMATS } from './common/app-date-formats';
import { SearchDialogComponent } from './component/search-dialog/search-dialog.component';
import { PhotoUploadDialogComponent } from './component/photo-upload-dialog/photo-upload-dialog.component';
import { TagDialogComponent } from './component/tag-dialog/tag-dialog.component';
import { ForgotPasswordDialogComponent } from './component/forgot-password-dialog/forgot-password-dialog.component';
import { ResetPasswordComponent } from './component/reset-password/reset-password.component';

@NgModule({
	declarations: [
		AppComponent,
		LoginComponent,
		HeaderComponent,
		SignupComponent,
		ProfileComponent,
		TimelineComponent,
		LogoutComponent,
		VerifyEmailComponent,
		MessageComponent,
		ErrorPageComponent,
		PostComponent,
		PostLikeDialogComponent,
		PostShareDialogComponent,
		PostCommentDialogComponent,
		CommentLikeDialogComponent,
		ConfirmationDialogComponent,
		ShareConfirmDialogComponent,
		SnackbarComponent,
		FollowingFollowerListDialogComponent,
		ViewPhotoDialogComponent,
		PostDialogComponent,
		PostDetailComponent,
		WaitingDialogComponent,
		SettingsComponent,
  SearchDialogComponent,
  PhotoUploadDialogComponent,
  TagDialogComponent,
  ForgotPasswordDialogComponent,
  ResetPasswordComponent
	],
	imports: [
		BrowserModule,
		AppRoutingModule,
		HttpClientModule,
		BrowserAnimationsModule,
		FormsModule,
		ReactiveFormsModule,
		MatToolbarModule,
		MatIconModule,
		MatButtonModule,
		MatMenuModule,
		MatCardModule,
		MatInputModule,
		MatFormFieldModule,
		MatProgressBarModule,
		MatProgressSpinnerModule,
		MatDividerModule,
		MatListModule,
		MatTooltipModule,
		MatChipsModule,
		MatBadgeModule,
		MatDialogModule,
		MatSnackBarModule,
		MatRippleModule,
		MatTabsModule,
		MatSelectModule,
		MatRadioModule,
		MatDatepickerModule,
		MatNativeDateModule,
		MatMomentDateModule
	],
	providers: [
		AuthGuard,
		{ provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
		{ provide: MAT_DATE_FORMATS, useValue: APP_DATE_FORMATS }
	],
	bootstrap: [AppComponent]
})
export class AppModule { }
