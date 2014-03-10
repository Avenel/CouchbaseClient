function (doc, meta) {
	if(doc.Type && doc.Type == "country") {
		emit("bla", doc.Code);
	}
}
