import { Component, Inject, OnInit } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

@Component({
	selector: 'app-snackbar',
	templateUrl: './snackbar.component.html',
	styleUrls: ['./snackbar.component.css']
})
export class SnackbarComponent implements OnInit {

	constructor(@Inject(MAT_SNACK_BAR_DATA) public data: string) { }

	ngOnInit(): void {
	}

}
