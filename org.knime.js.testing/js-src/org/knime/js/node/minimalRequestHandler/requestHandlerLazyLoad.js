if (requestHandler) {
	requestHandler.initRequest = function(request, textArea, representation, curRequests) {
		
		/* this is where the magic happens */
		var promise = knimeService.requestViewUpdate(request, representation.keepOrder);
		promise.progress(monitor => requestHandler.displayProgress(monitor))
			.then(response => requestHandler.displayResponse(response))
			.catch(error => requestHandler.displayError(request.sequence, error));
		/* end magic */
		
		if (promise.monitor && promise.monitor.requestSequence) {
			curRequests.push(promise);
			var text = "Issued request sequences: [";
			for (var i = 0; i < curRequests.length; i++) {
				text += curRequests[i].monitor.requestSequence;
				if (i < curRequests.length - 1) {
					text += ", ";
				}
			}
			text += "]\n";
			textArea.value += text;
			textArea.scrollTop = textArea.scrollHeight;
		}
		
	}
}