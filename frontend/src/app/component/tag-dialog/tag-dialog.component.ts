import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-tag-dialog',
	templateUrl: './tag-dialog.component.html',
	styleUrls: ['./tag-dialog.component.css']
})
export class TagDialogComponent implements OnInit {
	tagFormGroup: FormGroup;

	constructor(
		private thisDialogRef: MatDialogRef<TagDialogComponent>,
		private formBuilder: FormBuilder) { }

	get name() { return this.tagFormGroup.get('name'); } 

	ngOnInit(): void {
		this.tagFormGroup = this.formBuilder.group({
			name: new FormControl('', [Validators.minLength(3), Validators.maxLength(64)])
		});
	}

	addTag(e: Event): void {
		e.preventDefault();
		this.thisDialogRef.close({tagName: this.name.value});
		this.tagFormGroup.reset();
		Object.keys(this.tagFormGroup.controls).forEach(key => {
			this.tagFormGroup.get(key).setErrors(null) ;
		});
	}
}
