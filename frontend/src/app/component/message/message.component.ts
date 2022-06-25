import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppConstants } from 'src/app/common/app-constants';

@Component({
	selector: 'app-message',
	templateUrl: './message.component.html',
	styleUrls: ['./message.component.css']
})
export class MessageComponent implements OnInit, OnDestroy {
	messageType: string;
	messageHeader: string;
	messageDetail: string;
	toSignup: boolean = false;
	toLogin: boolean = false;

	constructor(private router: Router) { }

	ngOnInit(): void {
		this.messageType = localStorage.getItem(AppConstants.messageTypeLabel)
		this.messageHeader = localStorage.getItem(AppConstants.messageHeaderLabel)
		this.messageDetail = localStorage.getItem(AppConstants.messageDetailLabel)
		this.toSignup = localStorage.getItem(AppConstants.toSignupLabel) === 'true' ? true : false;
		this.toLogin = localStorage.getItem(AppConstants.toLoginLabel) === 'true' ? true : false;

		if (this.messageType === null || this.messageHeader === null) {
			this.router.navigateByUrl('/');
		}
	}

	ngOnDestroy(): void {
		localStorage.removeItem(AppConstants.messageTypeLabel);
		localStorage.removeItem(AppConstants.messageHeaderLabel);
		localStorage.removeItem(AppConstants.messageDetailLabel);
		localStorage.removeItem(AppConstants.toSignupLabel);
		localStorage.removeItem(AppConstants.toLoginLabel);
	}
}
