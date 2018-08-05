/**
 * CopyRight(c) this is a temporary header
 * Must be updated
 */
package com.linty.sonar.plugins.vhdlrc.a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.config.Configuration;
import com.linty.sonar.plugins.vhdlrc.Vhdl;


public class VhdlTest {

	@Test
	public void test_default_settings() {
		MapSettings settings = new MapSettings();
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	@Test
	public void test_unset_suffixs_should_return_default_suffixes() {
		MapSettings settings = new MapSettings();
		settings.setProperty("sonar.vhdl.file.suffixes", "");
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	@Test
	public void test_empty_suffixs_should_return_default_suffixes() {
		MapSettings settings = new MapSettings();
		settings.setProperty("sonar.vhdl.file.suffixes", ",");
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	@Test
	public void test_custom_suffixs_should_return_custom() {
		MapSettings settings = new MapSettings();
		settings.setProperty("sonar.vhdl.file.suffixes", ".vhd,.txt,.jpeg");
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).hasSize(3);
		assertThat(v.getFileSuffixes()).containsExactly(".vhd",".txt",".jpeg");
	}
	
	@Test
	public void test_null_return_from_config_should_return_default_suffixes() {
		NullConfig nc = new NullConfig();
		Vhdl v = new Vhdl(nc);
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	protected class NullConfig implements Configuration{
		 @Override
		 public  String[] getStringArray(String key) {
			 return new String[] {null,null};
		 }

		@Override
		public Optional<String> get(String key) {
			return null;
		}

		@Override
		public boolean hasKey(String key) {
			return false;
		}
	}

}
