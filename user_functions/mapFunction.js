function(doc, meta) {
	if (doc.Type == "city") {
		emit(doc.CountryCode, 2);
	}
}
