function (doc, meta) {
	if(doc.Type && doc.Type == "country") {
		emit(doc.Code);
	}
}