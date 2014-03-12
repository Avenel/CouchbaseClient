function(key, values, rereduce) {
	var total = 0.0;
	for (v in values) {
		total = total + v;
	}

	return total;
}
