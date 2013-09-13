function (doc, meta) {
	if(doc.type && doc.type == "beer") {
		emit(doc.name);
	}
}