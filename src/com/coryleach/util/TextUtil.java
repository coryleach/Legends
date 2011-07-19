package com.coryleach.util;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TextUtil {
	public static String titleize(String str) {
            /*String line = ChatColor.GOLD + repeat("_", 58);
            String center = ".[ " + ChatColor.YELLOW + str + ChatColor.GOLD + " ].";
            int pivot = line.length() / 2;
            int eatLeft = center.length() / 2;
            int eatRight = center.length() - eatLeft;
            return line.substring(0, pivot - eatLeft) + center + line.substring(pivot + eatRight);*/
            return titleize(str,ChatColor.GOLD,ChatColor.YELLOW);
	}

	public static String titleize(String str, ChatColor color1, ChatColor color2) {
		String line = color1 + repeat("_", 58);
		String center = "=[ " + color2 + str + color1 + " ]=";
		int pivot = line.length() / 2;
		int eatLeft = center.length() / 2;
		int eatRight = center.length() - eatLeft;
		return line.substring(0, pivot - eatLeft) + center + line.substring(pivot + eatRight);
	}
	
	public static String repeat(String s, int times) {
	    if (times <= 0) return "";
	    else return s + repeat(s, times-1);
	}

	/*
	public static ArrayList<String> split(String str) {
		return new ArrayList<String>(Arrays.asList(str.trim().split("\\s+")));
	}
	
	public static String implode(List<String> list, String glue) {
	    String ret = "";
	    for (int i=0; i<list.size(); i++) {
	        if (i!=0) {
	        	ret += glue;
	        }
	        ret += list.get(i);
	    }
	    return ret;
	}
	public static String implode(List<String> list) {
		return implode(list, " ");
	}*/
		
	/*public static String commandHelp(List<String> aliases, String param, String desc) {
		ArrayList<String> parts = new ArrayList<String>();
		parts.add(Conf.colorCommand+Conf.aliasBase.get(0));
		parts.add(TextUtil.implode(aliases, ", "));
		if (param.length() > 0) {
			parts.add(Conf.colorParameter+param);
		}
		if (desc.length() > 0) {
			parts.add(Conf.colorSystem+desc);
		}
		//Log.debug(TextUtil.implode(parts, " "));
		return TextUtil.implode(parts, " ");
	}*/

    
	public static String getMaterialName(Material material) {
		String ret = material.toString();
		ret = ret.replace('_', ' ');
		ret = ret.toLowerCase();
		return ret.substring(0, 1).toUpperCase()+ret.substring(1);
	}

	/// TODO create tag whitelist!!
	public  static ArrayList<String> substanceChars = new ArrayList<String>(Arrays.asList(new String []{
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", 
			"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", 
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", 
			"s", "t", "u", "v", "w", "x", "y", "z"
			}));
			
	public static String getComparisonString(String str) {
		String ret = "";
		
		for (char c : str.toCharArray()) {
			if (substanceChars.contains(String.valueOf(c))) {
				ret += c;
			}
		}
		
		return ret.toLowerCase();
	}
}


