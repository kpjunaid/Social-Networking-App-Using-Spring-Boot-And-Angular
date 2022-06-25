import { Injectable } from '@angular/core';
import { PostService } from './post.service';

@Injectable({
	providedIn: 'root'
})
export class DialogService {

	constructor(private postService: PostService) { }

	
}
