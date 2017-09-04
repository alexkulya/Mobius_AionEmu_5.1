/*
 * This file is part of the Aion-Emu project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.utils;

import java.nio.ByteBuffer;

/**
 * @author -Nemesiss-
 */
public class Util
{
	public static void printSection(String sectionName)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("-[ " + sectionName + " ]");
		while (sb.length() < 79)
		{
			sb.insert(0, "=");
		}
		System.out.println(sb.toString());
	}
	
	public static void printProgressBarHeader(int size)
	{
		final StringBuilder header = new StringBuilder("0%[");
		for (int i = 0; i < size; i++)
		{
			header.append("-");
		}
		header.append("]100%");
		System.out.println(header);
		System.out.print("   ");
	}
	
	public static void printCurrentProgress()
	{
		System.out.print("+");
	}
	
	public static void printEndProgress()
	{
		System.out.print("+ Done\n");
	}
	
	/**
	 * Convert data from given ByteBuffer to hex
	 * @param data
	 * @return hex
	 */
	public static String toHex(ByteBuffer data)
	{
		final StringBuilder result = new StringBuilder();
		int counter = 0;
		int b;
		while (data.hasRemaining())
		{
			if ((counter % 16) == 0)
			{
				result.append(String.format("%04X: ", counter));
			}
			
			b = data.get() & 0xff;
			result.append(String.format("%02X ", b));
			
			counter++;
			if ((counter % 16) == 0)
			{
				result.append("  ");
				toText(data, result, 16);
				result.append("\n");
			}
		}
		final int rest = counter % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); i++)
			{
				result.append("   ");
			}
			toText(data, result, rest);
		}
		return result.toString();
	}
	
	/**
	 * Gets last <tt>cnt</tt> read bytes from the <tt>data</tt> buffer and puts into <tt>result</tt> buffer in special format:
	 * <ul>
	 * <li>if byte represents char from partition 0x1F to 0x80 (which are normal ascii chars) then it's put into buffer as it is</li>
	 * <li>otherwise dot is put into buffer</li>
	 * </ul>
	 * @param data
	 * @param result
	 * @param cnt
	 */
	private static void toText(ByteBuffer data, StringBuilder result, int cnt)
	{
		int charPos = data.position() - cnt;
		for (int a = 0; a < cnt; a++)
		{
			final int c = data.get(charPos++);
			if ((c > 0x1f) && (c < 0x80))
			{
				result.append((char) c);
			}
			else
			{
				result.append('.');
			}
		}
	}
	
	/**
	 * Converts name to valid pattern For example : "atracer" -> "Atracer"
	 * @param name
	 * @return String
	 */
	public static String convertName(String name)
	{
		if (!name.isEmpty())
		{
			return name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
		}
		return "";
	}
}