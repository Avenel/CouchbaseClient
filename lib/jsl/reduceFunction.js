function (key, values, rereduce) {
	if (!rereduce) {
		return(parseInt(values.length));
	} else {
		var total = 0.0;
		for (v in values) {
			total += parseInt(v);
		}
		return(parseInt(total));
	}
}
