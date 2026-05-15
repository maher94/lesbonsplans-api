package com.lebonplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest (
		 @NotBlank(message = "...")
		    @Size(max = 1000)
		    String content	){

	 

}
