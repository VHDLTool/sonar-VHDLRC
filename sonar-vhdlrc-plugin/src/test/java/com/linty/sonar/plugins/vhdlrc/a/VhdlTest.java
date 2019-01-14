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
		settings.setProperty("sonar.vhdlrc.file.suffixes", "");
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	@Test
	public void test_empty_suffixs_should_return_default_suffixes() {
		MapSettings settings = new MapSettings();
		settings.setProperty("sonar.vhdlrc.file.suffixes", ",");
		Vhdl v = new Vhdl(settings.asConfig());
		assertThat(v.getFileSuffixes()).containsExactly(".vhdl",".vhd");
	}
	
	@Test
	public void test_custom_suffixs_should_return_custom() {
		MapSettings settings = new MapSettings();
		settings.setProperty("sonar.vhdlrc.file.suffixes", ".vhd,.txt,.jpeg");
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
