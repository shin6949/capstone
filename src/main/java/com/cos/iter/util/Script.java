package com.cos.iter.util;

public class Script {
	public static String alert(String msg) {
		return "<script>" +
				"alert('" + msg + "');" +
				"</script>";
	}
	
	public static String back(String msg) {
		return "<script>" +
				"alert('" + msg + "');" +
				"history.back();" +
				"</script>";
	}

	public static String href(String msg, String href) {
		return "<script>" +
				"alert('" + msg + "');" +
				"location.href=\"" + href + "\";" +
				"</script>";
	}
}
