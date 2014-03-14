function(doc, meta) {
	if (doc.Type == "country") {
		emit(doc.Continent, parseFloat(doc.LifeExpectancy));
	}
}
