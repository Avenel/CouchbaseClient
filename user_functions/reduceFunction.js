function (keys, values, rereduce) {
	var total = 0;
	var count = 0;

	if (!rereduce) {
		for (v in values) {
		   total += values[v];
		   count++;
		}
		
		// we need to save the amount of items to have access on it in the rereduce process.
		return {count: count, total: total};
	} else {
		for (v in values) {
		   total += values[v]['total'];
		   count += values[v]['count'];
		}    
		
		// if count == 0, couchbase just return null
	  	var average = total / count;
		return (average);	
	}
}

