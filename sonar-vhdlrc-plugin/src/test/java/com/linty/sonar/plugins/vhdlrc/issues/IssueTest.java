/**
 * CopyRight(c) this is a temporary header
 * Must be updated
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
