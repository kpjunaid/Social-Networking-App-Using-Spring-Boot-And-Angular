import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
	selector: 'app-waiting-dialog',
	templateUrl: './waiting-dialog.component.html',
	styleUrls: ['./waiting-dialog.component.css']
})
export class WaitingDialogComponent implements OnInit {

	constructor(@Inject(MAT_DIALOG_DATA) public dataHeader: string) { }

	ngOnInit(): void {
	}

}
