import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './component/login/login.component';
import { LogoutComponent } from './component/logout/logout.component';
import { MessageComponent } from './component/message/message.component';
import { PostDetailComponent } from './component/post-detail/post-detail.component';
import { ProfileComponent } from './component/profile/profile.component';
import { ResetPasswordComponent } from './component/reset-password/reset-password.component';
import { SettingsComponent } from './component/settings/settings.component';
import { SignupComponent } from './component/signup/signup.component';
import { TimelineComponent } from './component/timeline/timeline.component';
import { VerifyEmailComponent } from './component/verify-email/verify-email.component';

const routes: Routes = [
	{ path: '', component: TimelineComponent },
	{ path: 'posts/tags/:tagName', component: TimelineComponent },
	{ path: 'profile', component: ProfileComponent },
	{ path: 'users/:userId', component: ProfileComponent },
	{ path: 'signup', component: SignupComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'logout', component: LogoutComponent },
	{ path: 'settings', component: SettingsComponent },
	{ path: 'verify-email/:token', component: VerifyEmailComponent },
	{ path: 'reset-password/:token', component: ResetPasswordComponent },
	{ path: 'posts/:postId', component: PostDetailComponent },
	{ path: 'message', component: MessageComponent },
	{ path: '**', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
	imports: [RouterModule.forRoot(routes)],
	exports: [RouterModule]
})
export class AppRoutingModule { }
