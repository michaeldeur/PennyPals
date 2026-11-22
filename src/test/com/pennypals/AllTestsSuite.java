package com.pennypals;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.pennypals")
@IncludeClassNamePatterns(".*Tests?")
public class AllTestsSuite {
}