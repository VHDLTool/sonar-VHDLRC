/*
 * Vhdl RuleChecker (Vhdl-rc) plugin for Sonarqube & Zamiacad
 * Copyright (C) 2019 Maxime Facquet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.linty.sonar.plugins.vhdlrc.issues;


import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class IssueTest {

	@Test
	public void test() {
		Issue i = new Issue();
		i.errorMsg = "error msg";
		i.file = Paths.get("src/test");
		assertThat(i.errorMsg()).isNotEmpty();
		assertThat(i.file()).exists();
		assertThat(i.line()).isEqualTo(0);
		assertThat(i.ruleKey()).isNull();
		assertThat(i.remediationMsg()).isNull();
	}

}
