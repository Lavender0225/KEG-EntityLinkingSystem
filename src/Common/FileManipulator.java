package Common;

import java.io.*;
import java.util.*;

public class FileManipulator {
	public static HashSet<String> loadHashSetFromFile(String fileName)
			throws IOException {
		HashSet<String> set = new HashSet<String>();
		Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(
				fileName)));
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (line.length() > 0)
				set.add(line);
		}
		in.close();
		return set;
	}

	public static void outputCollectionFromFile(Collection<String> collection,
			String fileName) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				fileName)));
		for (String element : collection) {
			out.println(element);
			out.flush();
		}
		out.close();
	}

	public static HashMap<String, HashSet<String>> loadOneToMany(
			String fileName, String separator1, String separator2)
			throws IOException {
		HashMap<String, HashSet<String>> oneToMany = new HashMap<String, HashSet<String>>();

		//System.out.println("Start loading " + fileName);

		Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(
				fileName)));
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			String[] oneMany = line.split(separator1);

			if (oneMany.length != 2)
				continue;

			String one = oneMany[0];
			if (one.length() == 0)
				continue;
			if (oneMany[1].endsWith(";"))
				oneMany[1] = oneMany[1].substring(0, oneMany[1].length() - 1);
			String[] many = oneMany[1].split(separator2);

			HashSet<String> temp = new HashSet<String>();
			for (String each : many)
				temp.add(each);
			oneToMany.put(one, temp);
		}
		in.close();

		//System.out.println("Finish loading " + fileName);

		return oneToMany;
	}

	public static void outputStringList(
			List<String> resultList, String fileName) throws IOException {
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				fileName)));
		for(String tmp : resultList) {
			pw.println(tmp);
			pw.flush();
		}
		pw.println();
		pw.close();
		//System.out.println("Finish output " + fileName);
	}
	
	public static void outputStringHashMap(
			HashMap<String, String> hsahMap, String fileName,
			String separator1) throws IOException {
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				fileName)));
		for (Map.Entry<String, String> entry : hsahMap.entrySet()) {
			String key = entry.getKey();
			if (key.length() == 0)
				continue;
			String value = entry.getValue();
			
			pw.println(key + separator1 + value);
			pw.flush();
		}
		pw.close();
		//System.out.println("Finish output " + fileName);
	}
}
