/*
 * Copyright (c) 2013 Malhar Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.sql;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupByOperator extends SqlOperator {
	/**
	 * Group By name list
	 */
	ArrayList<String> groupByNames = new ArrayList<String>();

	/**
	 * Process rows operator.
	 */
	@Override
	public ArrayList<HashMap<String, Object>> processRows(
			ArrayList<HashMap<String, Object>> rows) {
		return null;
	}
	
	/**
	 * Add group by column name.
	 */
	public void addGroupByName(String name) {
		groupByNames.add(name);
	}
}